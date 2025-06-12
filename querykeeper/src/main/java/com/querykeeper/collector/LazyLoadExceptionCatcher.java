package com.querykeeper.collector;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LazyLoadExceptionCatcher {
    private static final Logger log = LoggerFactory.getLogger(LazyLoadExceptionCatcher.class);

    @AfterThrowing(pointcut = "execution(* com..*(..))", throwing = "ex")
    public void afterThrowingLazy(Exception ex) {
        if (!(ex instanceof LazyInitializationException)) {
            return;
        }

        String message = ex.getMessage();
        Pattern p = Pattern.compile(".*: ([\\w\\.]+)\\.([\\w]+).*");
        java.util.regex.Matcher m = p.matcher(message);

        if (!m.matches()) {
            log.error("\n[QueryKeeper] ▶ LazyLoadException X Unmatched message format: {}", message);
            return;
        }

        String className = m.group(1);
        String fieldName = m.group(2);

        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getDeclaredField(fieldName);
            Class<?> targetType = resolveCollectionGenericType(field);

            if (targetType != null) {
                String entityName = targetType.getSimpleName();
                QueryKeeperContext.getCurrent().markLazyLoadException(entityName);

                log.warn("\n[QueryKeeper] ▶ LazyLoadException (!) DETECTED - Entity: {}, Field: {}", className,
                        fieldName);
            }
        } catch (Exception e) {
            log.error("\n[QueryKeeper] ▶ LazyLoadException X FAILED to extract entity from: {}", message);
        }
    }

    private Class<?> resolveCollectionGenericType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?> argType) {
                return argType;
            }
        }
        return null;
    }
}
