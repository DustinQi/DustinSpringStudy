package com.dustin.framework;

/**
 * @auther qimy 实现该接口的bean需要在bean装载和依赖注入完成后，做一些验证（具体验证内容可以自定义）
 * @date 2020/10/15
 * Description
 */
public interface InitializingBean {

    void afterPropertiesSet();
}
