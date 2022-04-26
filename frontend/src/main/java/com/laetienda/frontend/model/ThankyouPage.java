package com.laetienda.frontend.model;

public class ThankyouPage {

    private String key;
    private String source;
    private String title;
    private String description;
    private String actionLink;
    private String actionText;

    public ThankyouPage(String key, String source, String title, String description, String actionLink, String actionText) {
        this.key = key;
        this.source = source;
        this.title = title;
        this.description = description;
        this.actionLink = actionLink;
        this.actionText = actionText;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionLink() {
        return actionLink;
    }

    public void setActionLink(String actionLink) {
        this.actionLink = actionLink;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }
}
