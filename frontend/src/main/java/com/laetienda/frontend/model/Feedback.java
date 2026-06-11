package com.laetienda.frontend.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Feedback {

    private Map<String, Set<String>> errors;
    private Map<String, Set<String>> success;
    private Set<String> globalErrors;
    private Set<String> globalSuccess;

    public Feedback() {
    }

    public boolean hasErrors(String field){
        if(errors == null)
            return false;

        return errors.containsKey(field) && !errors.get(field).isEmpty();
    }

    public void addError(String field, String message){
        if(errors == null)
            errors = new HashMap<>();

        if(!errors.containsKey(field)) {
            Set<String> set = new HashSet<>();
            errors.put(field, set);
        }

        errors.get(field).add(message);
    }

    public Set<String> getErrors(String field){
        if(errors == null)
            return null;

        return errors.get(field);
    }

    public boolean hasSuccess(String field){
        if(success == null)
            return false;

        if(hasErrors(field))
            return false;

        return success.containsKey(field) && !success.get(field).isEmpty();
    }

    public void addSuccess(String field, String message){
        if(success == null)
            success = new HashMap<>();

        if(!success.containsKey(field)) {
            Set<String> set = new HashSet<>();
            success.put(field, set);
        }

        success.get(field).add(message);
    }

    public Set<String> getSuccess(String field){
        if(success == null)
            success = new HashMap<>();

        return success.get(field);
    }

    public boolean hasGlobalErrors(){
        if(globalErrors == null)
            return false;

        return !globalErrors.isEmpty();
    }

    public void addGlobalError(String message){
        if(globalErrors == null)
            globalErrors = new HashSet<>();

        globalErrors.add(message);
    }

    public boolean hasGlobalSuccess(){
        if(globalSuccess == null)
            return false;

        return !globalSuccess.isEmpty();
    }

    public void addGlobalSuccess(String message){
        if(globalSuccess == null)
            globalSuccess = new HashSet<>();

        globalSuccess.add(message);
    }
}
