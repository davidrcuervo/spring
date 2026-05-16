package com.laetienda.lib.options;

public enum CompanyMemberPolicy implements InputOptions{
    PUBLIC("Public", "Anyone can join without restrictions"),
    REGISTRATION_REQUIRED("Registration Required", "Users must register before joining"),
    AUTHORIZATION_REQUIRED("Authorization Required", "Users must be authorized by an admin to join");

    private final String label;
    private final String description;

    CompanyMemberPolicy(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getName(){
        return this.name();
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
