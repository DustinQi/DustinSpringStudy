package com.dustin.framework;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther qimy
 * @date 2020/10/15
 * Description
 */
public class DustinApplicationContext {

    private Class configClass;
    // 存放beanDefinition
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String, BeanDefinition>();
    // 存放单例bean
    private Map<String, Object> singletonObjects = new HashMap<String, Object>();

    public DustinApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描:通过扫描.class文件得到beanDefinitionMap
        scan(configClass);

        // 创建非lazy加载的单例bean
        createNonLazySingleton();
    }

    private void createNonLazySingleton() {
        for(String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton") && !beanDefinition.isLazy()){
                // 创建一个bean
                Object bean = createBean(beanDefinition, beanName);
                // 存入单例池
                singletonObjects.put(beanName, bean);
            }
        }
    }

    /**
     * 创建一个bean
     * @return
     */
    private Object createBean(BeanDefinition beanDefinition, String beanName) {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            // 通过反射获取实例
            Object instance = beanClass.getDeclaredConstructor().newInstance();
            // 这里可以有一些其它逻辑, 例如下面的依赖注入
            // 将有@Autowired注解的类注入：被依赖类作为属性进行填充(Spring中这部分逻辑由AutowiredAnnotationBeanPostProcessor实现)
            for(Field field : beanClass.getDeclaredFields()){  // 通过反射，遍历此类中的所有field
                if (field.isAnnotationPresent(Autowired.class)) {  // 如果某个field上有@Autowired注解，说明此field为被依赖的bean，将此field对应的bean作为属性填充
                    // 先byType，再byName(能保证找到正确的bean，例如某个奇葩故意把UserService的注解写成@Component("OrderService")......)
                    // byType的方式这里先省略，简化为根据byName寻找
                    Object bean = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(instance, bean);
                }
            }
            // 如果实现了BeanNameAware接口，需要设置其beanName
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过扫描.class文件得到beanDefinitionMap。注意并非获得了bean，而只是class文件得到beanDefinitionMap
     * @param configClass
     */
    private void scan(Class configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            // 注解的value是包名"com.dustin.service"。真正需要扫描和加载的是.java文件编译之后的.class文件（在写好service层后提前build一下项目）
            String path = componentScanAnnotation.value();

            ClassLoader classLoader = DustinApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource("com/dustin/service");
            // 为了解释resource是什么，这里可以打印一下resource的内容
            File file = new File(resource.getFile());
            for(File f : file.listFiles()){
                // service类编译后对应的.class文件的完整路径
                System.out.println(f);
            }
            // 逐个加载.class文件（也就是bean）
            for(File f : file.listFiles()){
                String s = f.getAbsolutePath();
                if(s.endsWith(".class")){  // 根据文件类型筛
                    s = s.substring(s.indexOf("com"), s.indexOf(".class"));
                    s = s.replace("\\", ".");  // 获得bean的包名，如"com.dustin.service.UserService"
                    try {
                        Class clazz = classLoader.loadClass(s);
                        System.out.println(clazz);
                        // 因为只加载非lazy加载的单例bean，所以需要对各个bean的属性进行判断。进而引申出BeanDefinition
                        // 设置每个bean的beanDefinition
                        if(clazz.isAnnotationPresent(Component.class)){
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setBeanClass(clazz);
                            // 从注解中解析出beanName
                            Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();
                            // 有@Lazy注解的是懒加载
                            if(clazz.isAnnotationPresent(Lazy.class)){
                                beanDefinition.setLazy(false);
                            }
                            // 有@Scope注解的是非单例
                            if(clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                                String value = scopeAnnotation.value();
                                beanDefinition.setScope(value);
                            }else{
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * 给client端返回一个bean
     * @param beanName
     * @return
     */
    public Object getBean(String beanName){
        if(!beanDefinitionMap.containsKey(beanName)){
            throw new NullPointerException();
        } else {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 单例bean和非单例bean的获取方式不同
            if(beanDefinition.getScope().equals("singleton")){
                // 单例池获取
                Object bean = singletonObjects.get(beanName);
                if(null == bean){
                    bean = createBean(beanDefinition, beanName);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            } else if (beanDefinition.getScope().equals("prototype")){
                // new一个新bean
                return createBean(beanDefinition, beanName);
            }
        }
        return null;
    }
}
