package com.laetienda.lib.annotation;

import com.laetienda.lib.options.HtmlInputType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface HtmlInput {

    String label();
    String placeholder();
    HtmlInputType type() default HtmlInputType.TEXT;
    boolean required() default true;
    String style_size() default "col-md-6";
}
