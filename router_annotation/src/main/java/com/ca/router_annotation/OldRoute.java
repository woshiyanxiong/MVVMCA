package com.ca.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>创建时间：2023/12/21/021</p>
 *
 * @author yanxiong
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface OldRoute {
    String path();
}
