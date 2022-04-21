package com.laetienda.frontend.model;

import com.laetienda.lib.options.*;

import java.util.List;

public class Form {

    private String name;
    private String url;
    private HtmlFormMethod method;
    private HtmlFormAction action;
    private List<Input> inputs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HtmlFormMethod getMethod() {
        return method;
    }

    public void setMethod(HtmlFormMethod method) {
        this.method = method;
    }

    public HtmlFormAction getAction() {
        return action;
    }

    public void setAction(HtmlFormAction action) {
        this.action = action;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }
}
