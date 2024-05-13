package com.tmotions.wms.models.tokenexpiremodel;

import java.util.ArrayList;

public class TokenExpireModel {

    public int StatusCode;

    public String Message;

    public ArrayList<Object> Errors;

    public TokenDataModel Data;

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

    public TokenDataModel getData() {
        return Data;
    }

    public void setData(TokenDataModel data) {
        Data = data;
    }
}