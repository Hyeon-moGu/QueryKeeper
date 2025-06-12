package com.querykeeper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asserts that no unexpected lazy-loading occurs during method execution.
 *
 * Useful for detecting and preventing implicit fetches of lazy-loaded
 * associations, which can lead to performance issues or
 * LazyInitializationExceptions.
 *
 * This annotation can be used to either:
 * <ul>
 * <li>Count how many times a lazy-loaded field is accessed</li>
 * <li>Fail the test if a {@code LazyInitializationException} is thrown</li>
 * </ul>
 *
 * <p>
 * <b>entity</b>: Optional simple name of the entity to monitor (e.g. "User").
 * If omitted, all entities are monitored.
 * </p>
 * <p>
 * <b>maxCount</b>: Maximum number of allowed lazy-load accesses.
 * Default is 0 (fail on any lazy load).
 * </p>
 * <p>
 * <b>includeException</b>: Whether to fail the test if a
 * {@code LazyInitializationException}
 * occurs. Default is true.
 * </p>
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectLazyLoad or
 * {@code @ExpectLazyLoad(entity = "Order", maxCount = 1, includeException =
 * false)}
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpectLazyLoad {
    String entity() default "";

    int maxCount() default 0;

    boolean includeException() default true;
}
