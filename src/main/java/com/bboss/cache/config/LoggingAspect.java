package com.bboss.cache.config;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.bboss.cache.controller..*(..))")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;
        // 记录info日志
        StringBuilder logInfo = new StringBuilder();
        logInfo.append(":\n");
        logInfo.append("+===================================== Request Start ========================================\n");
        // 记录请求uri
        logInfo.append("| URL            : ").append(request.getRequestURL().toString()).append("\n");
        // 记录请求的方式
        logInfo.append("| HTTP Method    : ").append(request.getMethod()).append("\n");
        // 记录类名和方法名
        logInfo.append("| Class Method   : ").append(joinPoint.getSignature().getDeclaringTypeName())
                .append(".").append(joinPoint.getSignature().getName()).append("\n");
        // 记录请求参数
        logInfo.append("| Request Args   : ").append(getParam(joinPoint)).append("\n");
        // 请求耗时
        logInfo.append("| Time taken     : ").append(timeTaken).append("ms").append("\n");
        // 返回参数
        logInfo.append("| Result         : ").append(result).append("\n");
        logInfo.append("+====================================== Request END =========================================\n");
        log.info(logInfo.toString());
        return result;
    }

    /**
     * 获取参数名和参数值
     * @param joinPoint
     * @return 返回JSON结构字符串
     */
    public String getParam(JoinPoint joinPoint) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        Object[] values = joinPoint.getArgs();
        String[] names = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return JSONObject.toJSONString(map);
    }
}