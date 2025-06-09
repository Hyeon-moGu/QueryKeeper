package com.querysentinel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h4>Fails the test if any database access occurs during method
 * execution.</h4>
 *
 * Useful for verifying that the code does not hit the database at all—
 * such as when testing pure logic, caching, or mocking scenarios.
 *
 * This assertion fails if any JDBC query is executed during the test.
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectNoDb}
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExpectNoDb {
}
