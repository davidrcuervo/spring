package com.laetienda.lib.options;

public enum DbServiceAccessPolicy {
    NO_SERVICE("No service can read or edit item. Only authorized real users can edit or read items"),
    SERVICE_READ("Service can read item"),
    SERVICE_WRITE("Service can read and write item");

    private final String description;

    DbServiceAccessPolicy(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }
}
