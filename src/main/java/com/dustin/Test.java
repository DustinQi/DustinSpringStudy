package com.dustin;

import com.dustin.framework.DustinApplicationContext;
import com.dustin.service.UserService;

/**
 * @auther qimy
 * @date 2020/10/15
 * Description
 */
public class Test {
    public static void main(String[] args) {
        // 启动，扫描包路径，创建bean(非lazy加载的单例bean)
        DustinApplicationContext dustinApplicationContext = new DustinApplicationContext(AppConfig.class);
        // 默认获取单例bean，这里可以打印验证下:
        UserService userService1 = (UserService)dustinApplicationContext.getBean("userService");
        Object userService2 = dustinApplicationContext.getBean("userService");
        System.out.println(userService1);
        System.out.println(userService2);
        // 如果有@Scope("prototype")注解，则非单例：
        Object orderService1 = dustinApplicationContext.getBean("orderService");
        Object orderService2 = dustinApplicationContext.getBean("orderService");
        System.out.println(orderService1);
        System.out.println(orderService2);

        userService1.testAutowired();
    }
}
