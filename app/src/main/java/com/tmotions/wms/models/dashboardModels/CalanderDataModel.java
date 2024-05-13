package com.tmotions.wms.models.dashboardModels;

public class CalanderDataModel {
    public int DateNumber;
    public int LeaveTypeID;
    public double NoOfDays;

    public String Date;
    public String LeaveAbbreviation;
    public String LeaveCategory;
    public String LeaveType;
    public String RequestStatus;

    public int getDateNumber() {
        return DateNumber;
    }

    public void setDateNumber(int dateNumber) {
        DateNumber = dateNumber;
    }

    public int getLeaveTypeID() {
        return LeaveTypeID;
    }

    public void setLeaveTypeID(int leaveTypeID) {
        LeaveTypeID = leaveTypeID;
    }

    public double getNoOfDays() {
        return NoOfDays;
    }

    public void setNoOfDays(double noOfDays) {
        NoOfDays = noOfDays;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getLeaveAbbreviation() {
        return LeaveAbbreviation;
    }

    public void setLeaveAbbreviation(String leaveAbbreviation) {
        LeaveAbbreviation = leaveAbbreviation;
    }

    public String getLeaveCategory() {
        return LeaveCategory;
    }

    public void setLeaveCategory(String leaveCategory) {
        LeaveCategory = leaveCategory;
    }

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getRequestStatus() {
        return RequestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        RequestStatus = requestStatus;
    }
}
