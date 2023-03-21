package com.laetienda.model.messager;

import org.hibernate.validator.constraints.UniqueElements;

import jakarta.validation.constraints.*;
import java.util.*;

public class EmailMessage {

    @NotNull
    private String subject;

    @NotNull
    private String view;
    private Map<String, Object> variables;

    @NotNull
    @Size(min=1, max=10)
    @UniqueElements
    private Set<@Email String> to;

    @Size(min=1, max=10) @UniqueElements
    private Set<@Email String> cc;

    @Size(min=1, max=10) @UniqueElements
    private Set<@Email String> bc;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Set<String> getTo() {
        return to;
    }

    public void setTo(Set<String> to) {
        this.to = to;
    }

    public Set<String> getCc() {
        return cc;
    }

    public void setCc(Set<String> cc) {
        this.cc = cc;
    }

    public Set<String> getBc() {
        return bc;
    }

    public void setBc(Set<String> bc) {
        this.bc = bc;
    }

    public void addToAddress(String address){
        if(to == null){
            to = new HashSet<>();
        }

        to.add(address);
    }

    public void addCcAddress(String address){
        if(cc == null){
            cc = new HashSet<>();
        }

        cc.add(address);
    }

    public void addBcAddress(String address){
        if(bc == null){
            bc = new HashSet<>();
        }

        bc.add(address);
    }
}
