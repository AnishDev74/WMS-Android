package com.tmotions.wms.models;

import java.util.ArrayList;

public class SentOtpResponseModel {
    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;

    public LoginModel data;

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

    public LoginModel getData() {
        return data;
    }

    public void setData(LoginModel data) {
        this.data = data;
    }
}
