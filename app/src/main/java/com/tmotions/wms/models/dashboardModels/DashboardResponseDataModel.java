package com.tmotions.wms.models.dashboardModels;

import java.util.ArrayList;

public class DashboardResponseDataModel{
    public ArrayList<CalanderDataModel> leaveCalenderModels;
    public ArrayList<LeaveBalanceModel> leaveBalanceModels;
    public String Access_token;
    public int notificationCount;

    public ArrayList<CalanderDataModel> getLeaveCalenderModels() {
        return leaveCalenderModels;
    }

    public void setLeaveCalenderModels(ArrayList<CalanderDataModel> leaveCalenderModels) {
        this.leaveCalenderModels = leaveCalenderModels;
    }

    public ArrayList<LeaveBalanceModel> getLeaveBalanceModels() {
        return leaveBalanceModels;
    }

    public void setLeaveBalanceModels(ArrayList<LeaveBalanceModel> leaveBalanceModels) {
        this.leaveBalanceModels = leaveBalanceModels;
    }

    public String getAccess_token() {
        return Access_token;
    }

    public void setAccess_token(String access_token) {
        Access_token = access_token;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
