package com.dustin.framework;

/**
 * @auther qimy 实现了这个接口的类需要手动设置beanName
 * @date 2020/10/15
 * Description
 */
public interface BeanNameAware {

    void setBeanName(String name);
}
