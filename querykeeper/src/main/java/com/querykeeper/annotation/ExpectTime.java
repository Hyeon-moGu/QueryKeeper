package com.querykeeper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Measures the execution time of the test method.
 *
 * Fails the test if the execution time exceeds the specified threshold.
 * If no threshold is defined (i.e. value = 0), the test will not fail,
 * but a warning will be logged and the duration will still be reported.
 *
 * <ul>
 * <li><b>value</b>: Maximum allowed execution time in milliseconds</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectTime(300)} → Fails if execution takes longer than
 * 300ms
 * </p>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpectTime {
    long value() default 0;
}
