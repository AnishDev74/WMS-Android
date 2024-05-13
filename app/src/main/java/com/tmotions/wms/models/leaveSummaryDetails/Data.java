package com.tmotions.wms.models.leaveSummaryDetails;

public class Data{
    public String LeaveSummaryDetailId,LeaveType,LeaveCategory,FromDate,ToDate,FromSession,ToSession,RequestStatus,ApproverName,AppliedOn,Reason,OfficeLocation;

    public int EmployeeId,LeaveTypeID;

    public double NoOfDays;
    public EL EL;
    public CL CL;
    public CO CO;
    public RH RH;
    public SL SL;

    public String getLeaveSummaryDetailId() {
        return LeaveSummaryDetailId;
    }

    public void setLeaveSummaryDetailId(String leaveSummaryDetailId) {
        LeaveSummaryDetailId = leaveSummaryDetailId;
    }

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getLeaveCategory() {
        return LeaveCategory;
    }

    public void setLeaveCategory(String leaveCategory) {
        LeaveCategory = leaveCategory;
    }

    public String getFromDate() {
        return FromDate;
    }

    public void setFromDate(String fromDate) {
        FromDate = fromDate;
    }

    public String getToDate() {
        return ToDate;
    }

    public void setToDate(String toDate) {
        ToDate = toDate;
    }

    public String getFromSession() {
        return FromSession;
    }

    public void setFromSession(String fromSession) {
        FromSession = fromSession;
    }

    public String getToSession() {
        return ToSession;
    }

    public void setToSession(String toSession) {
        ToSession = toSession;
    }

    public String getRequestStatus() {
        return RequestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        RequestStatus = requestStatus;
    }

    public String getApproverName() {
        return ApproverName;
    }

    public void setApproverName(String approverName) {
        ApproverName = approverName;
    }

    public String getAppliedOn() {
        return AppliedOn;
    }

    public void setAppliedOn(String appliedOn) {
        AppliedOn = appliedOn;
    }

    public int getEmployeeId() {
        return EmployeeId;
    }

    public void setEmployeeId(int employeeId) {
        EmployeeId = employeeId;
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

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public com.tmotions.wms.models.leaveSummaryDetails.EL getEL() {
        return EL;
    }

    public void setEL(com.tmotions.wms.models.leaveSummaryDetails.EL EL) {
        this.EL = EL;
    }

    public com.tmotions.wms.models.leaveSummaryDetails.CL getCL() {
        return CL;
    }

    public void setCL(com.tmotions.wms.models.leaveSummaryDetails.CL CL) {
        this.CL = CL;
    }

    public com.tmotions.wms.models.leaveSummaryDetails.CO getCO() {
        return CO;
    }

    public void setCO(com.tmotions.wms.models.leaveSummaryDetails.CO CO) {
        this.CO = CO;
    }

    public com.tmotions.wms.models.leaveSummaryDetails.RH getRH() {
        return RH;
    }

    public void setRH(com.tmotions.wms.models.leaveSummaryDetails.RH RH) {
        this.RH = RH;
    }

    public com.tmotions.wms.models.leaveSummaryDetails.SL getSL() {
        return SL;
    }

    public void setSL(com.tmotions.wms.models.leaveSummaryDetails.SL SL) {
        this.SL = SL;
    }

    public String getOfficeLocation() {
        return OfficeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        OfficeLocation = officeLocation;
    }
}
