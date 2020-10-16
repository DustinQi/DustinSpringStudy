package com.dustin.service;

import com.dustin.framework.Autowired;
import com.dustin.framework.BeanNameAware;
import com.dustin.framework.Component;

/**
 * @auther qimy
 * @date 2020/10/15
 * Description
 */
@Component("userService")
public class UserService implements BeanNameAware {

    // @Autowired注解实现自动注入的功能
    @Autowired
    private OrderService orderService;

    private String name;

    public void testAutowired(){
        System.out.println("testAutowired: " + orderService);
    }

    public void setBeanName(String name) {
        this.name = name;
    }
}
