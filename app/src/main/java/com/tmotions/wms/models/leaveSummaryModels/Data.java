package com.tmotions.wms.models.leaveSummaryModels;

import java.util.ArrayList;

public class Data{
    public ArrayList<LeaveSummary> LeaveSummary;
    public ArrayList<Object> LeaveSummaryDetail;
    public String Access_token;

    public ArrayList<com.tmotions.wms.models.leaveSummaryModels.LeaveSummary> getLeaveSummary() {
        return LeaveSummary;
    }

    public void setLeaveSummary(ArrayList<com.tmotions.wms.models.leaveSummaryModels.LeaveSummary> leaveSummary) {
        LeaveSummary = leaveSummary;
    }

    public ArrayList<Object> getLeaveSummaryDetail() {
        return LeaveSummaryDetail;
    }

    public void setLeaveSummaryDetail(ArrayList<Object> leaveSummaryDetail) {
        LeaveSummaryDetail = leaveSummaryDetail;
    }

    public String getAccess_token() {
        return Access_token;
    }

    public void setAccess_token(String access_token) {
        Access_token = access_token;
    }
}
