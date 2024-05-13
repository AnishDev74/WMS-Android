package com.tmotions.wms.models.leaveSummaryModels;

public class LeaveSummary{

    public int EmployeeId;
    public String EmployeeName;
    public String LeaveType;
    public String LeaveDay;
    public String LeaveStatus;
    public String LeaveCategory;
    public String Guid;

    private boolean isChecked;

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getLeaveDay() {
        return LeaveDay;
    }

    public void setLeaveDay(String leaveDay) {
        LeaveDay = leaveDay;
    }

    public String getLeaveStatus() {
        return LeaveStatus;
    }

    public void setLeaveStatus(String leaveStatus) {
        LeaveStatus = leaveStatus;
    }

    public String getGuid() {
        return Guid;
    }

    public void setGuid(String guid) {
        Guid = guid;
    }

    public int getEmployeeId() {
        return EmployeeId;
    }

    public void setEmployeeId(int employeeId) {
        EmployeeId = employeeId;
    }

    public String getEmployeeName() {
        return EmployeeName;
    }

    public void setEmployeeName(String employeeName) {
        EmployeeName = employeeName;
    }

    public String getLeaveCategory() {
        return LeaveCategory;
    }

    public void setLeaveCategory(String leaveCategory) {
        LeaveCategory = leaveCategory;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
