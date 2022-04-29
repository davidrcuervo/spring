package com.laetienda.lib.model;

import java.util.*;

public class Mistake {

    private Date timestamp;
    private int status;
    private Map<String, List<String>> errors;

    public Mistake(){
        timestamp = new Date();
        errors = new HashMap<String, List<String>>();
    }

    public Mistake(int status){
        timestamp = new Date();
        errors = new HashMap<String, List<String>>();
    }

    public void add(String key, String message){
        List<String> mistakes = errors.get(key);

        if(mistakes == null){
            mistakes = new ArrayList<String>();
            errors.put(key, mistakes);
        }

        mistakes.add(message);
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}
