package com.laetienda.lib.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Mistake {

    private Date timestamp;
    private int status;
    private Map<String, List<String>> errors;

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
