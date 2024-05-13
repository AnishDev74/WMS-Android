package com.tmotions.wms.models.tokenexpiremodel;

import java.util.ArrayList;

public class TokenDataModel {

    public String Access_Token;
    public String Refresh_Token;

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
}