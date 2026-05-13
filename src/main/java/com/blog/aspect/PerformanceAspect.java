package com.blog.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Enhanced Performance Aspect to monitor execution time and register Micrometer metrics.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);
    private final MeterRegistry meterRegistry;

    public PerformanceAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.blog.service.*.*(..)) || execution(* com.blog.controller.rest.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().toShortString();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        // Timer for latency monitoring
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object proceed = joinPoint.proceed();
            
            // Record success metric
            sample.stop(Timer.builder("blog.api.response.time")
                    .tag("class", className)
                    .tag("method", joinPoint.getSignature().getName())
                    .tag("uri", name)
                    .register(meterRegistry));
            
            meterRegistry.counter("blog.api.requests.total", "status", "success", "uri", name).increment();
            
            return proceed;
        } catch (Throwable throwable) {
            // Record failure metric
            meterRegistry.counter("blog.api.requests.total", "status", "error", "uri", name).increment();
            throw throwable;
        }
    }
}
