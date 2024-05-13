package com.tmotions.wms.models;

public class HolidayModel {

    private Integer id;
    private double count;
    private Integer publicHolidayCount;
    private Integer holidayTypeId;
    private String countryName;
    private String LeaveType;
    private String Leavecategory;
    private String LeaveAbbreviation;
    private String date;
    private String publicHolidayColourCode;
    private String requestLeaveColourCode;
    private String holidayName;
    private String holidayDescription;
    private String holidayDate;
    private String holidayType;
    private String colourCode;

    private String RequestStatus;
    private Boolean isActive;
    private Boolean isLeave;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHolidayTypeId() {
        return holidayTypeId;
    }

    public void setHolidayTypeId(Integer holidayTypeId) {
        this.holidayTypeId = holidayTypeId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getHolidayDescription() {
        return holidayDescription;
    }

    public void setHolidayDescription(String holidayDescription) {
        this.holidayDescription = holidayDescription;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getHolidayType() {
        return holidayType;
    }

    public void setHolidayType(String holidayType) {
        this.holidayType = holidayType;
    }

    public String getColourCode() {
        return colourCode;
    }

    public void setColourCode(String colourCode) {
        this.colourCode = colourCode;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPublicHolidayCount() {
        return publicHolidayCount;
    }

    public void setPublicHolidayCount(Integer publicHolidayCount) {
        this.publicHolidayCount = publicHolidayCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPublicHolidayColourCode() {
        return publicHolidayColourCode;
    }

    public void setPublicHolidayColourCode(String publicHolidayColourCode) {
        this.publicHolidayColourCode = publicHolidayColourCode;
    }

    public String getRequestLeaveColourCode() {
        return requestLeaveColourCode;
    }

    public void setRequestLeaveColourCode(String requestLeaveColourCode) {
        this.requestLeaveColourCode = requestLeaveColourCode;
    }

    public Boolean getLeave() {
        return isLeave;
    }

    public void setLeave(Boolean leave) {
        isLeave = leave;
    }

    public String getLeaveType() {
        return LeaveType;
    }

    public void setLeaveType(String leaveType) {
        LeaveType = leaveType;
    }

    public String getLeavecategory() {
        return Leavecategory;
    }

    public void setLeavecategory(String leavecategory) {
        Leavecategory = leavecategory;
    }

    public String getLeaveAbbreviation() {
        return LeaveAbbreviation;
    }

    public void setLeaveAbbreviation(String leaveAbbreviation) {
        LeaveAbbreviation = leaveAbbreviation;
    }

    public String getRequestStatus() {
        return RequestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        RequestStatus = requestStatus;
    }
}