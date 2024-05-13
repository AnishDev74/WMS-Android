package com.tmotions.wms.models.filtermodels;


import java.util.ArrayList;
import java.util.List;
public class Data{
    public ArrayList<LeaveList> ManagerList;
    public ArrayList<LeaveList> LeaveList;
    public ArrayList<LeaveList> SessionList;
    public ArrayList<LeaveList> StatusList;

    public ArrayList<LeaveList> ResourceList;
    public String Access_token;
    public String ApplyingTo;

    public String ManagerEmail;

    public String ShortLeaveBalance;

    public ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> getManagerList() {
        return ManagerList;
    }

    public void setManagerList(ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> managerList) {
        ManagerList = managerList;
    }

    public ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> getLeaveList() {
        return LeaveList;
    }

    public void setLeaveList(ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> leaveList) {
        LeaveList = leaveList;
    }

    public ArrayList<LeaveList> getSessionList() {
        return SessionList;
    }

    public void setSessionList(ArrayList<LeaveList> sessionList) {
        SessionList = sessionList;
    }

    public ArrayList<LeaveList> getStatusList() {
        return StatusList;
    }

    public void setStatusList(ArrayList<LeaveList> statusList) {
        StatusList = statusList;
    }

    public String getAccess_token() {
        return Access_token;
    }

    public void setAccess_token(String access_token) {
        Access_token = access_token;
    }

    public String getApplyingTo() {
        return ApplyingTo;
    }

    public void setApplyingTo(String applyingTo) {
        ApplyingTo = applyingTo;
    }

    public String getManagerEmail() {
        return ManagerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        ManagerEmail = managerEmail;
    }

    public ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> getResourceList() {
        return ResourceList;
    }

    public void setResourceList(ArrayList<com.tmotions.wms.models.filtermodels.LeaveList> resourceList) {
        ResourceList = resourceList;
    }

    public String getShortLeaveBalance() {
        return ShortLeaveBalance;
    }

    public void setShortLeaveBalance(String shortLeaveBalance) {
        ShortLeaveBalance = shortLeaveBalance;
    }
}
