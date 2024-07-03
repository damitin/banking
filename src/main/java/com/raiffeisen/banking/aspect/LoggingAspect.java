package com.raiffeisen.banking.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Before(value = "allMethodsFromServicePackage()")
    public void beforeAnyServiceMethodAdvice(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        LOGGER.info(">> before - {}() - {}", methodName, Arrays.toString(args));
    }

    @After(value = "allMethodsFromServicePackage()")
    public void afterAnyServiceMethodAdvice(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        LOGGER.info(">> after - {}() - {}", methodName, Arrays.toString(args));
    }

    @Pointcut("execution(* com.raiffeisen.banking.service..* (..))")
    private void allMethodsFromServicePackage() {

    }
}
