package com.tmotions.wms.models.wfhresourcelist;

import java.util.ArrayList;

public class ResourceListResponse {
    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;
    public ArrayList<Data> Data;

    public int getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(int statusCode) {
        StatusCode = statusCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public ArrayList<Object> getErrors() {
        return Errors;
    }

    public void setErrors(ArrayList<Object> errors) {
        Errors = errors;
    }

    public ArrayList<com.tmotions.wms.models.wfhresourcelist.Data> getData() {
        return Data;
    }

    public void setData(ArrayList<com.tmotions.wms.models.wfhresourcelist.Data> data) {
        Data = data;
    }
}
