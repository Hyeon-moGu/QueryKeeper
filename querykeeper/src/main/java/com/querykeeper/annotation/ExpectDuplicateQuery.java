package com.querykeeper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fails the test if identical SQL queries (including parameters) are executed
 * more than allowed.
 *
 * Useful for detecting performance issues like duplicate queries in loops,
 * N+1 problems, or redundant data access within the same test method.
 *
 * This assertion checks for duplicate SQL statements where both the SQL string
 * and parameter values match exactly.
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectDuplicateQuery(max = 0)}
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExpectDuplicateQuery {
    int max() default 0;
}
