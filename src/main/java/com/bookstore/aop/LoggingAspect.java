package com.bookstore.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(public * com.bookstore.service..*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        try {
            log.info("Executing: {} ", methodName);

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;

            log.info("{} executed in {}ms", methodName, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("Service method {} failed after {}ms with exception {}",
                    methodName,
                    duration,
                    e.getClass().getSimpleName());

            throw e;
        }
    }
}
