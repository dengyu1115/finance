package com.nature.common.aop.aspect;

import com.nature.common.exception.Warn;
import com.nature.common.model.Res;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class WebAspect {

    @Pointcut("@annotation(com.nature.common.aop.annotation.Web)")
    public void pointcut() {
        // ignore
    }

    @Around("pointcut()")
    public Res<?> around(ProceedingJoinPoint p) {
        MethodSignature sign = (MethodSignature) p.getSignature();
        Method method = sign.getMethod();
        String name = method.getName();
        Class<?> cls = p.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(cls + "." + name);
        try {
            logger.info("start {}", this.argsInfo(method.getParameters(), p.getArgs()));
            Res<?> res = (Res<?>) p.proceed();
            logger.info("end {}", res);
            return res;
        } catch (Warn e) {
            logger.warn("warn {}", e.getMessage());
            return Res.warn(e.getMessage());
        } catch (Throwable t) {
            logger.error("error", t);
            return Res.error();
        }
    }

    private String argsInfo(Parameter[] params, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder("args ");
        for (int i = 0; i < args.length; i++) {
            builder.append(params[i].getName()).append("=").append(args[i]).append(" ");
        }
        return builder.toString();
    }
}
