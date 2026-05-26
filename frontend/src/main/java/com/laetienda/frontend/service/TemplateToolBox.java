package com.laetienda.frontend.service;

import java.util.List;

public interface TemplateToolBox {
    String href(String index, Object... uriVariables);
    String getUri();
    String getPath();
    boolean isActive(String link);
    List<String> getSegments();
}
