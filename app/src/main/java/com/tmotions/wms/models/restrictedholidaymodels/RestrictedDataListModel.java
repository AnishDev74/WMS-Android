package com.tmotions.wms.models.restrictedholidaymodels;

public class RestrictedDataListModel {
    public String Holiday;
    public String Date;
    public int Balance;
    public String FromDate;

    public Boolean Selected;

    public String getHoliday() {
        return Holiday;
    }

    public void setHoliday(String holiday) {
        Holiday = holiday;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getBalance() {
        return Balance;
    }

    public void setBalance(int balance) {
        Balance = balance;
    }

    public String getFromDate() {
        return FromDate;
    }

    public void setFromDate(String fromDate) {
        FromDate = fromDate;
    }

    public Boolean getSelected() {
        return Selected;
    }

    public void setSelected(Boolean selected) {
        Selected = selected;
    }
}
