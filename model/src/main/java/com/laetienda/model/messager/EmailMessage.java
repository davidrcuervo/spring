package com.laetienda.model.messager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laetienda.model.company.Friend;
import com.laetienda.model.schema.DbItem;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.lang.reflect.Method;
import java.util.*;

public class EmailMessage {
    final static private Logger log = LoggerFactory.getLogger(EmailMessage.class);

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

    @JsonIgnore
    private DbItem item;
    private String clazzName;
    private String jsonItem;

    public EmailMessage() {
    }

    public EmailMessage(String view, String toEmailAddress, String subject) {
        this.setView(view);
        this.setSubject(subject);
        this.addToAddress(toEmailAddress);
    }

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

    public String getClazzName() {
        return clazzName;
    }

    private void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    @JsonIgnore
    public <T> void setItem(DbItem item, Class<T> clazz) {
        this.item = item;
        this.setClazzName(clazz.getName());
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T> T getItem() throws HttpServerErrorException {

        try {
            Class<T> clazz = (Class<T>) Class.forName(clazzName);
            return clazz.cast(item);
        } catch (ClassNotFoundException | ClassCastException e) {
            log.error("MODEL_EMAIL::getItem. $error: {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @JsonIgnore
    public DbItem getDbItem(){
        return this.item;
    }

    public String getJsonItem() {
        return jsonItem;
    }

    public void setJsonItem(String jsonItem) {
        this.jsonItem = jsonItem;
    }
}
