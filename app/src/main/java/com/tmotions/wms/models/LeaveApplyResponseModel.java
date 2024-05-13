package com.tmotions.wms.models;

import java.util.ArrayList;

public class LeaveApplyResponseModel {

    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;
    public ArrayList<Object> Data;

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

    public ArrayList<Object> getData() {
        return Data;
    }

    public void setData(ArrayList<Object> data) {
        Data = data;
    }
}
