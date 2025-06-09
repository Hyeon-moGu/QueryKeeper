package com.querysentinel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h4>Fails the test if a transaction is active during method execution.</h4>
 *
 * Useful for ensuring that the code does not unintentionally depend on a
 * transactional context.
 *
 * <ul>
 * <li><b>strict = false</b> (default): Allows read-only transactions, but fails
 * if any writable transaction is active.</li>
 * <li><b>strict = true</b>: Fails if any transaction is active, even if it's
 * read-only.</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b> {@code @ExpectNoTx} or {@code @ExpectNoTx(strict = true)}
 * </p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectNoTx {
    boolean strict() default false;
}