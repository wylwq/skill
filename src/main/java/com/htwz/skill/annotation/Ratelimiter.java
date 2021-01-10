package com.htwz.skill.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 19:44
 * @Version V1.0.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ratelimiter {

    /**
     * 以每秒固定的速率向桶中加令牌
     */
    double limit();

    /**
     * 在规定的时间内如果没有令牌，就走服务降级处理
     */
    long timeout();

}
