package com.tmotions.wms.models;

public class HolidayCalenderDataModel{
    public String HolidayType;
    public String Holiday;
    public String Day;
    public String Date;

    public boolean IsDisabled;

    public String getHolidayType() {
        return HolidayType;
    }

    public void setHolidayType(String holidayType) {
        HolidayType = holidayType;
    }

    public String getHoliday() {
        return Holiday;
    }

    public void setHoliday(String holiday) {
        Holiday = holiday;
    }

    public String getDay() {
        return Day;
    }

    public void setDay(String day) {
        Day = day;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public boolean isDisabled() {
        return IsDisabled;
    }

    public void setDisabled(boolean disabled) {
        IsDisabled = disabled;
    }
}
