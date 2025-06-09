package com.querysentinel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h4>Asserts that the number of SQL queries matches the specified
 * expectations.</h4>
 *
 * Useful for verifying that a method executes only the expected number of
 * SELECT, INSERT, UPDATE, and DELETE queries.
 *
 * If no values are specified (i.e. all default to -1), the test will not fail,
 * but query logs will still be printed for inspection.
 *
 * <ul>
 * <li><b>select</b>: Expected number of SELECT queries</li>
 * <li><b>insert</b>: Expected number of INSERT queries</li>
 * <li><b>update</b>: Expected number of UPDATE queries</li>
 * <li><b>delete</b>: Expected number of DELETE queries</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectQuery(select = 2, insert = 1)} or
 * {@code @ExpectQuery}
 * </p>
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectQuery {
    int select() default -1;

    int insert() default -1;

    int update() default -1;

    int delete() default -1;
}
