package com.blog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/*
@Aspect
@Component
public class SecurityLoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.blog.controller.rest.AuthController.authenticateUser(..))")
    public void logLoginAttempt(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            logger.info("Security Event: Login attempt for user: {}", args[0]);
        }
    }

    @AfterReturning("execution(* com.blog.controller.rest.AuthController.authenticateUser(..))")
    public void logLoginSuccess(JoinPoint joinPoint) {
        logger.info("Security Event: Login successful");
    }

    @AfterThrowing(pointcut = "execution(* com.blog.controller.rest.AuthController.authenticateUser(..))", throwing = "ex")
    public void logLoginFailure(JoinPoint joinPoint, Exception ex) {
        logger.warn("Security Event: Login failed: {}", ex.getMessage());
    }

    @Before("execution(* com.blog.config.JwtAuthenticationFilter.doFilterInternal(..))")
    public void logRequestAccess(JoinPoint joinPoint) {
        // Potentially log all requests, but maybe too noisy
        // logger.debug("Security Event: JWT validation attempt");
    }
}
*/
