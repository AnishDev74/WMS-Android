package com.tmotions.wms.models;

public class LeaveRequestModel {

 private String LeaveType, StartDate, EndDate, SessionStart, SessionEnd, MobileNumber, ApplyTo, ManagerEmail, Remarks, WorkLocation,
         Guid,Status,Reason,CcManagerEmail,TimeZone,ApplyForEmail,ApplyForName;
 private int ApplyForId;

 private boolean IsHalfday;

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public String getSessionStart() {
        return SessionStart;
    }

    public void setSessionStart(String sessionStart) {
        SessionStart = sessionStart;
    }

    public String getSessionEnd() {
        return SessionEnd;
    }

    public void setSessionEnd(String sessionEnd) {
        SessionEnd = sessionEnd;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }

    public String getApplyTo() {
        return ApplyTo;
    }

    public void setApplyTo(String applyTo) {
        ApplyTo = applyTo;
    }

    public String getManagerEmail() {
        return ManagerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        ManagerEmail = managerEmail;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }

    public String getWorkLocation() {
        return WorkLocation;
    }

    public void setWorkLocation(String workLocation) {
        WorkLocation = workLocation;
    }

    public String getGuid() {
        return Guid;
    }

    public void setGuid(String guid) {
        Guid = guid;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public String getCcManagerEmail() {
        return CcManagerEmail;
    }

    public void setCcManagerEmail(String ccManagerEmail) {
        CcManagerEmail = ccManagerEmail;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public void setTimeZone(String timeZone) {
        TimeZone = timeZone;
    }

    public String getApplyForEmail() {
        return ApplyForEmail;
    }

    public void setApplyForEmail(String applyForEmail) {
        ApplyForEmail = applyForEmail;
    }

    public int getApplyForId() {
        return ApplyForId;
    }

    public void setApplyForId(int applyForId) {
        ApplyForId = applyForId;
    }

    public String getApplyForName() {
        return ApplyForName;
    }

    public void setApplyForName(String applyForName) {
        ApplyForName = applyForName;
    }

    public boolean isHalfday() {
        return IsHalfday;
    }

    public void setHalfday(boolean halfday) {
        IsHalfday = halfday;
    }
}
