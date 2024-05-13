package com.tmotions.wms.models.leaveSummaryDetails;


import java.util.ArrayList;

public class LeaveSummaryDetailsResponse {

    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;
    public Data Data;

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

    public com.tmotions.wms.models.leaveSummaryDetails.Data getData() {
        return Data;
    }

    public void setData(com.tmotions.wms.models.leaveSummaryDetails.Data data) {
        Data = data;
    }
}
