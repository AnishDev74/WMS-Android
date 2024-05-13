package com.tmotions.wms.models.notificationmodels;


import java.util.ArrayList;

public class Data{
    public ArrayList<NotificationList> NotificationList;

    public ArrayList<com.tmotions.wms.models.notificationmodels.NotificationList> getNotificationList() {
        return NotificationList;
    }

    public void setNotificationList(ArrayList<com.tmotions.wms.models.notificationmodels.NotificationList> notificationList) {
        NotificationList = notificationList;
    }
}
