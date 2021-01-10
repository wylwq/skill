package com.htwz.skill.aop;

import com.google.common.util.concurrent.RateLimiter;
import com.htwz.skill.annotation.Ratelimiter;
import com.htwz.skill.util.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 采用aop的方式实现限流
 * @Author ly
 * @Date 2020/4/2 7:35
 * @Version V1.0.0
 **/
@Aspect
@Component
@Slf4j
public class RateLimteAop extends BaseAop{

    private Map<String, RateLimiter> curMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.htwz.skill.annotation.Ratelimiter)")
    public void roundLimteAop() {

    }

    @Around(value = "roundLimteAop()")
    public Object doBefore(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = getMethod(proceedingJoinPoint);
        if (method == null) {
            return null;
        }
        Ratelimiter ratelimiter = method.getDeclaredAnnotation(Ratelimiter.class);
        if (ratelimiter == null) {
            //直接就如实际请求中
            return proceedingJoinPoint.proceed();
        }
        //2.调用谷歌工具类的Ratelimit创建令牌
        double limit = ratelimiter.limit();
        long timeout = ratelimiter.timeout();
        String requestURI = getRequestURI();
        RateLimiter rateLimiter = curMap.get(requestURI);
        if (null == rateLimiter) {
            rateLimiter = RateLimiter.create(limit);
            curMap.put(requestURI, rateLimiter);
        }
        boolean tryAcquire = rateLimiter.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        if (!tryAcquire) {
            log.error("请稍后再试~");
            throw new RuntimeException("当前系统参与人数过多，请稍后再试~");
        }
        return proceedingJoinPoint.proceed();
    }

    @Deprecated
    private void fallBack() {
        HttpServletResponse response = getResponse();
        response.setHeader("Content-Type", "text/html;charset=utf-8");
        try(PrintWriter writer = response.getWriter()) {
            writer.println("当前系统参与人数过多，请稍后再试~");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRequestURI() {
        String requestURI = getRequest().getRequestURI();
        String ipAddr = CodeUtil.getIpAddr(getRequest());
        StringBuffer path = new StringBuffer();
        path.append(ipAddr).append(":").append(requestURI);
        return path.toString();
    }

}
