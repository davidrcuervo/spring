package org.laetienda.library.annotation;

import org.laetienda.library.options.HtmlFormAction;
import org.laetienda.library.options.HtmlFormMethod;

public @interface HtmlForm {

    String name();
    HtmlFormMethod method() default HtmlFormMethod.POST;
    HtmlFormAction action() default HtmlFormAction.CREATE;
}
