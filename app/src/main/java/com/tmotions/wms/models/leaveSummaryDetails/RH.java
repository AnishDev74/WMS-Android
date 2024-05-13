package com.tmotions.wms.models.leaveSummaryDetails;

public class RH {
    public int LeaveTypeID;
    public String LeaveType;
    public String LeaveAbbreviation;
    public double Granted;
    public double Availed;
    public double Balance;
    public int CarryForwarded;

    public int getLeaveTypeID() {
        return LeaveTypeID;
    }

    public void setLeaveTypeID(int leaveTypeID) {
        LeaveTypeID = leaveTypeID;
    }

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getLeaveAbbreviation() {
        return LeaveAbbreviation;
    }

    public void setLeaveAbbreviation(String leaveAbbreviation) {
        LeaveAbbreviation = leaveAbbreviation;
    }

    public double getGranted() {
        return Granted;
    }

    public void setGranted(double granted) {
        Granted = granted;
    }

    public double getAvailed() {
        return Availed;
    }

    public void setAvailed(double availed) {
        Availed = availed;
    }

    public double getBalance() {
        return Balance;
    }

    public void setBalance(double balance) {
        Balance = balance;
    }

    public int getCarryForwarded() {
        return CarryForwarded;
    }

    public void setCarryForwarded(int carryForwarded) {
        CarryForwarded = carryForwarded;
    }
}
