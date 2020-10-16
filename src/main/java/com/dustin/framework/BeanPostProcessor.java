package com.dustin.framework;

/**
 * @auther qimy
 * @date 2020/10/15
 * Description
 * 这里简单介绍下BeanPostProcessor的作用。
 * 1、顾名思义，后置处理器，是在bean已经被DustinApplicationContext加载后生效的
 * 2、回顾下DustinApplicationContext类中的createBean方法，其中循环寻找类内的@Autowired注解，并进行依赖注入的逻辑，就可以理解成是一个BeanPostProcessor
 * 3、与@Autowired类似，@Resource也会对应一个后置处理器，用来将有@Resource注解的bean进行加载
 */
public interface BeanPostProcessor {

}
