package com.querykeeper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asserts that no LazyInitializationException occurs during method execution.
 *
 * This annotation is used to detect improper access to lazy-loaded associations
 * outside of a transactional context (i.e., when the entity is in a detached
 * state).
 *
 * <p>
 * LazyInitializationException typically indicates that a field marked for
 * lazy loading was accessed after the persistence context was closed.
 * This can lead to runtime errors and should be avoided in most applications.
 * </p>
 * 
 * <p>
 * <b>Usage:</b> {@code @ExpectDetachedAccess }
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpectDetachedAccess {
}
