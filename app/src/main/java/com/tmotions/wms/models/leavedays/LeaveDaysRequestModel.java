package com.tmotions.wms.models.leavedays;

public class LeaveDaysRequestModel {

    String LeaveType,StartDate,EndDate,SessionStart,SessionEnd,TimeZone;
    boolean IsOverTime;

    boolean IsHalfday;

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

    public boolean isOverTime() {
        return IsOverTime;
    }

    public void setOverTime(boolean overTime) {
        IsOverTime = overTime;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public void setTimeZone(String timeZone) {
        TimeZone = timeZone;
    }

    public boolean isHalfday() {
        return IsHalfday;
    }

    public void setHalfday(boolean halfday) {
        IsHalfday = halfday;
    }
}
