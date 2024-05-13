package com.tmotions.wms.models.dashboardModels;

import java.util.ArrayList;
import java.util.List;
public class DashboardResponseModel{
    public int StatusCode;
    public String Message;
    public ArrayList<Object> Errors;

    public DashboardResponseDataModel Data;

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

    public DashboardResponseDataModel getData() {
        return Data;
    }

    public void setData(DashboardResponseDataModel data) {
        Data = data;
    }
}
