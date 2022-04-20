package com.api.liargame.aop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class LoggingAspect {
    // @Before("execution(* com.api.liargame..*Controller.*(..))")
    // public void onBeforeHandler(JoinPoint joinPoint) {
    //     CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
    //     String[] parameterNames = codeSignature.getParameterNames();
    //     Object[] args = joinPoint.getArgs();
    //     Gson gson = new GsonBuilder().setPrettyPrinting().create();;
    //     String requestJson = gson.toJson(args[0]);
    //     log.info(requestJson);
    // }

    //모든 Controller에 적용
    @Around("execution(* com.api.liargame..*Controller.*(..))")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // -- request -- 
        String requestId = UUID.randomUUID().toString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(proceedingJoinPoint.getArgs()[0]);
        log.info("\nrequest : "+ requestId +"\n" + json);
        // -- request -- 
        Object result = proceedingJoinPoint.proceed();
        
        // -- response -- 
        String requestJson = gson.toJson(result);
        log.info("\nresponse: "+ requestId +"\n" + requestJson);
        return result;
    }
}