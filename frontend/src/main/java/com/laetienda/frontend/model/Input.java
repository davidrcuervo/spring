package com.laetienda.frontend.model;

import com.laetienda.lib.options.HtmlInputType;

public class Input {

    private String label;
    private String id;
    private String name;
    private String placeholder;
    private String style_size;
    private HtmlInputType type;
    private boolean required;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getStyle_size() {
        return style_size;
    }

    public void setStyle_size(String style_size) {
        this.style_size = style_size;
    }

    public HtmlInputType getType() {
        return type;
    }

    public void setType(HtmlInputType type) {
        this.type = type;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
