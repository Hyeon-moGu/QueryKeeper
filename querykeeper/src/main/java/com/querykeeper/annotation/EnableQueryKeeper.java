package com.querykeeper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.querykeeper.junit.QueryKeeperExtension;

/**
 * Enables QueryKeeper features for the test class.
 *
 * This annotation activates {@link QueryKeeperExtension}, which provides
 * query assertion and performance monitoring capabilities for test methods.
 *
 * <p>
 * When applied to a test class, the following annotations become active:
 * </p>
 * <ul>
 * <li>{@code @ExpectQuery} — Asserts expected SQL query counts</li>
 * <li>{@code @ExpectTime} — Fails if execution time exceeds threshold</li>
 * <li>{@code @ExpectNoDb} — Fails if any DB access occurs</li>
 * <li>{@code @ExpectNoTx} — Fails if a transaction is active</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b><br>
 * {@code @EnableQueryKeeper}<br>
 * {@code class MyTest { ... }}
 * </p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(QueryKeeperExtension.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public @interface EnableQueryKeeper {
}
