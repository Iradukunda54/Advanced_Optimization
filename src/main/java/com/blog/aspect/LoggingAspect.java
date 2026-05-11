package com.blog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.blog.service.*.*(..)) || execution(* com.blog.controller.*.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Entering: {}.{}() with arguments: {}", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }

    @After("execution(* com.blog.service.*.*(..)) || execution(* com.blog.controller.*.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("Exiting: {}.{}()", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }
}
