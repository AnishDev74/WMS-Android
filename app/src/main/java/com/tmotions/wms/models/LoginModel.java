package com.tmotions.wms.models;

public class LoginModel {

    private int ManagerId;
    private String Access_Token,Refresh_Token,UserId,UserGuid,Name,Mobile,EmailId,Gender,DOB,ManagerName,OfficeLocation,AlphaNumeric_Ecn,AlphaNumeric_Ecn_managerid;

    private boolean IsManager;

    public int getManagerId() {
        return ManagerId;
    }
    public void setManagerId(int managerId) {
        ManagerId = managerId;
    }

    public String getAccess_Token() {
        return Access_Token;
    }

    public void setAccess_Token(String access_Token) {
        Access_Token = access_Token;
    }
    public String getRefresh_Token() {
        return Refresh_Token;
    }
    public void setRefresh_Token(String refresh_Token) {
        Refresh_Token = refresh_Token;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserGuid() {
        return UserGuid;
    }

    public void setUserGuid(String userGuid) {
        UserGuid = userGuid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getEmailId() {
        return EmailId;
    }

    public void setEmailId(String emailId) {
        EmailId = emailId;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getManagerName() {
        return ManagerName;
    }

    public void setManagerName(String managerName) {
        ManagerName = managerName;
    }

    public boolean isManager() {
        return IsManager;
    }

    public void setManager(boolean manager) {
        IsManager = manager;
    }

    public String getOfficeLocation() {
        return OfficeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        OfficeLocation = officeLocation;
    }

    public String getAlphaNumeric_Ecn() {
        return AlphaNumeric_Ecn;
    }

    public void setAlphaNumeric_Ecn(String alphaNumeric_Ecn) {
        AlphaNumeric_Ecn = alphaNumeric_Ecn;
    }

    public String getAlphaNumeric_Ecn_managerid() {
        return AlphaNumeric_Ecn_managerid;
    }

    public void setAlphaNumeric_Ecn_managerid(String alphaNumeric_Ecn_managerid) {
        AlphaNumeric_Ecn_managerid = alphaNumeric_Ecn_managerid;
    }
}
