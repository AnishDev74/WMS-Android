package com.tmotions.wms.models.managerview;

import java.util.ArrayList;

public class Data {
    public ArrayList<LeaveTypesList> LeaveTypesList;
    public ArrayList<ResourceList> ResourceList;

    public ArrayList<com.tmotions.wms.models.managerview.LeaveTypesList> getLeaveTypesList() {
        return LeaveTypesList;
    }

    public void setLeaveTypesList(ArrayList<com.tmotions.wms.models.managerview.LeaveTypesList> leaveTypesList) {
        LeaveTypesList = leaveTypesList;
    }

    public ArrayList<com.tmotions.wms.models.managerview.ResourceList> getResourceList() {
        return ResourceList;
    }

    public void setResourceList(ArrayList<com.tmotions.wms.models.managerview.ResourceList> resourceList) {
        ResourceList = resourceList;
    }
}
