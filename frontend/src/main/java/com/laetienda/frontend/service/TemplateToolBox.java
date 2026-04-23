package com.laetienda.frontend.service;

public interface TemplateToolBox {
    String href(String index);

    String getUri();

    String getPath();

    boolean isActive(String link);
}
