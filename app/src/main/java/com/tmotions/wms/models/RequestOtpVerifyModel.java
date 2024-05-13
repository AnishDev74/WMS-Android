package com.tmotions.wms.models;

public class RequestOtpVerifyModel {

    private String EmailId,OTP,FirebaseDeviceToken;
    private boolean IsAndroiodDevice;

    public String getEmailId() {
        return EmailId;
    }

    public void setEmailId(String emailId) {
        EmailId = emailId;
    }

    public String getOTP() {
        return OTP;
    }

    public void setOTP(String OTP) {
        this.OTP = OTP;
    }

    public String getFirebaseDeviceToken() {
        return FirebaseDeviceToken;
    }

    public void setFirebaseDeviceToken(String firebaseDeviceToken) {
        FirebaseDeviceToken = firebaseDeviceToken;
    }

    public boolean isAndroiodDevice() {
        return IsAndroiodDevice;
    }

    public void setAndroiodDevice(boolean androiodDevice) {
        IsAndroiodDevice = androiodDevice;
    }
}
