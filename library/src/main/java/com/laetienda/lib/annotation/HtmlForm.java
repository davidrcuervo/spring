package com.laetienda.lib.annotation;

import com.laetienda.lib.options.HtmlFormAction;
import com.laetienda.lib.options.HtmlFormMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HtmlForm {
    String name();
    String url() default "#";
    HtmlFormMethod method() default HtmlFormMethod.POST;
    HtmlFormAction action() default HtmlFormAction.CREATE;
}
