package com.tmotions.wms.models.leavedays;

import java.util.ArrayList;

public class LeaveDaysResponseModel {

    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;
    public Data Data;

    public class Data{
        public double Days;
        public boolean IsSandwichapplied;
    }

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

    public LeaveDaysResponseModel.Data getData() {
        return Data;
    }

    public void setData(LeaveDaysResponseModel.Data data) {
        Data = data;
    }
}
