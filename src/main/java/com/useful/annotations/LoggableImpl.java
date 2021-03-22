package com.useful.annotations;


import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public class LoggableImpl {

    final Environment env;

    private final Gson gson;
    private long start;
    private final boolean isDev;

    public LoggableImpl(Environment env) {
        this.env = env;
        this.gson = new Gson();
        this.isDev = isDev();
    }

    @Before("@annotation(Loggable)")
    public void logBeforeInit(JoinPoint joinPoint) {
        final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        start = System.currentTimeMillis();
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Object[] args = joinPoint.getArgs();
        Map<String, String> typeValue = new HashMap<>();
        for (int index = 0; index < args.length; index++) {
            if (Objects.nonNull(args[index])) {
                typeValue.put(codeSignature.getParameterNames()[index], gson.toJson(args[index]));
            }
        }
        if (isDev) {
            logger.info("stage=init method={} with Params={}", getSimpleName(joinPoint), typeValue);
        } else {
            logger.warn("stage=init method={} with Params={}", getSimpleName(joinPoint), typeValue);
        }
    }

    @AfterReturning(pointcut = "@annotation(Loggable)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String toJson = "";
        if (isResponseLoggable(joinPoint)) toJson = " with return=" + gson.toJson(result);

        if (isDev) {
            logger.info("stage=end method={}{} and executionTime={} ms", getSimpleName(joinPoint),
                    toJson, getExecutionTime());
        } else {
            logger.warn("stage=end method={}{} and executionTime={} ms", getSimpleName(joinPoint),
                    toJson, getExecutionTime());
        }
    }

    private boolean isResponseLoggable(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);
        return loggable.logResponse();
    }

    @AfterThrowing(pointcut = "@annotation(Loggable)", throwing = "throwing")
    public void logAfterThrowing(JoinPoint joinPoint, final Throwable throwing) {
        final Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.error("stage=end method={} with error={} and cause={} and executionTime={} ms", getSimpleName(joinPoint),
                throwing.getClass().getName(), throwing.getCause(), getExecutionTime());
    }

    private String getSimpleName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType().getSimpleName()
                .concat(".").concat(joinPoint.getSignature().getName());
    }

    private long getExecutionTime() {
        return System.currentTimeMillis() - start;
    }

    private boolean isDev() {
        return Arrays.asList(env.getActiveProfiles()).contains("dev");
    }
}
