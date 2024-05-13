package com.tmotions.wms.models.notificationmodels;

import java.util.List; 
public class NotificationList{
    public String Sender;
    public int EmployeeId;
    public String Notification;
    public String Leave_Guid;
    public String NotificationDate;
    public boolean IsNotificationView;
    public String Time;
    public String NotificationGuid;

    public Boolean IsManagerView;

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public int getEmployeeId() {
        return EmployeeId;
    }

    public void setEmployeeId(int employeeId) {
        EmployeeId = employeeId;
    }

    public String getNotification() {
        return Notification;
    }

    public void setNotification(String notification) {
        Notification = notification;
    }

    public String getLeave_Guid() {
        return Leave_Guid;
    }

    public void setLeave_Guid(String leave_Guid) {
        Leave_Guid = leave_Guid;
    }

    public String getNotificationDate() {
        return NotificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        NotificationDate = notificationDate;
    }

    public boolean isNotificationView() {
        return IsNotificationView;
    }

    public void setNotificationView(boolean notificationView) {
        IsNotificationView = notificationView;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getNotificationGuid() {
        return NotificationGuid;
    }

    public void setNotificationGuid(String notificationGuid) {
        NotificationGuid = notificationGuid;
    }

    public Boolean getManagerView() {
        return IsManagerView;
    }

    public void setManagerView(Boolean managerView) {
        IsManagerView = managerView;
    }
}
