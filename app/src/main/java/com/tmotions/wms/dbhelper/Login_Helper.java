package com.tmotions.wms.dbhelper;

import android.content.Context;

import com.tmotions.wms.models.LoginModel;


public class Login_Helper {

    public static  boolean saveUserData(LoginModel loginModel, Context context){
        return Login_DataSource.getInstance().saveUserData(loginModel, context);
    }

    public static  boolean updateUserData(LoginModel loginModel, Context context){
        return Login_DataSource.getInstance().updateUserData(context,loginModel);
    }

    public static LoginModel getLogin(Context context){
        return Login_DataSource.getInstance().getLogin(context);
    }

    public static boolean signOut(Context context){
        return Login_DataSource.getInstance().signOut(context);
    }

    public static boolean updateAccessAndRefreshToken(Context context, String userId, String accessToken, String refreshToken){
        return Login_DataSource.getInstance().updateAccessAndRefreshToken(context,userId,accessToken,refreshToken);
    }
}
