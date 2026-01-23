package com.turkcell.soccer.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.turkcell.soccer.controller..*(..)) || execution(* com.turkcell.soccer.service..*(..))")
    public void controllerAndServicePackage(){}

    @Around("controllerAndServicePackage()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();

        log.info(">> START: {}.{}() - Arguments: {}", className, methodName, Arrays.toString(args));

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            log.error("!ERROR: {}.{}() inside: {}", className, methodName, e.getMessage());
            throw e;
        }

        long executionTime = System.currentTimeMillis() - start;

        log.info("<< END: {}.{}() - Time: {} ms - Response: {}", className, methodName, executionTime, result);

        return result;

    }


}
