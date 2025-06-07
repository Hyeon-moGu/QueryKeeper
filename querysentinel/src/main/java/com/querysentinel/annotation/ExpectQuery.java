package com.querysentinel.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpectQuery {
    int select() default -1;
    int insert() default -1;
    int update() default -1;
    int delete() default -1;
}
