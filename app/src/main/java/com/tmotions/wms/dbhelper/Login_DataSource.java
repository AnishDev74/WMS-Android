package com.tmotions.wms.dbhelper;

import static com.tmotions.wms.dbhelper.Database_Helper.ACCESSTOKEN;
import static com.tmotions.wms.dbhelper.Database_Helper.ALPHANUMERIC_ENC;
import static com.tmotions.wms.dbhelper.Database_Helper.ALPHANUMERIC_ENC_MANAGER;
import static com.tmotions.wms.dbhelper.Database_Helper.DOB;
import static com.tmotions.wms.dbhelper.Database_Helper.EMAIL;
import static com.tmotions.wms.dbhelper.Database_Helper.GENDER;
import static com.tmotions.wms.dbhelper.Database_Helper.ISMANAGER;
import static com.tmotions.wms.dbhelper.Database_Helper.MANAGERID;
import static com.tmotions.wms.dbhelper.Database_Helper.MANAGERNAME;
import static com.tmotions.wms.dbhelper.Database_Helper.MOBILENUMBER;
import static com.tmotions.wms.dbhelper.Database_Helper.NAME;
import static com.tmotions.wms.dbhelper.Database_Helper.OFFICELOCATION;
import static com.tmotions.wms.dbhelper.Database_Helper.REFRESHTOKEN;
import static com.tmotions.wms.dbhelper.Database_Helper.USERID;
import static com.tmotions.wms.dbhelper.Database_Helper.USER_LOGIN;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tmotions.wms.models.LoginModel;

public class Login_DataSource {

    private static boolean hasObject = false;
    private static  Login_DataSource loginDataSource = null;

    public static  Login_DataSource getInstance() {
        if (hasObject) {
            return loginDataSource;
        } else {
            hasObject = true;
            loginDataSource = new  Login_DataSource();
            return loginDataSource;
        }
    }

    private static SQLiteDatabase getSQLiteDb(boolean isWritable, Context context) {
        Database_Helper databaseHelper = Database_Helper.getInstance(context);
        if (isWritable) {
            return databaseHelper.getWritableDatabase();
        } else {
            return databaseHelper.getReadableDatabase();
        }
    }

    boolean saveUserData(LoginModel loginModel, Context context) {
        boolean a = false;
        synchronized (Database_Helper.lock) {
            try {
                ContentValues values = new ContentValues();
                values.put(USERID, loginModel.getUserId());
                values.put(NAME, loginModel.getName());
                values.put(EMAIL, loginModel.getEmailId());
                values.put(MOBILENUMBER, loginModel.getMobile());
                values.put(DOB, loginModel.getDOB());
                values.put(GENDER, loginModel.getGender());
                values.put(ACCESSTOKEN, "Bearer "+loginModel.getAccess_Token());
                values.put(REFRESHTOKEN, loginModel.getRefresh_Token());
                values.put(MANAGERNAME, loginModel.getManagerName());
                values.put(MANAGERID, loginModel.getManagerId());
                values.put(ISMANAGER, String.valueOf(loginModel.isManager()));
                values.put(OFFICELOCATION, loginModel.getOfficeLocation());
                values.put(ALPHANUMERIC_ENC, loginModel.getAlphaNumeric_Ecn());
                values.put(ALPHANUMERIC_ENC_MANAGER, loginModel.getAlphaNumeric_Ecn_managerid());

                SQLiteDatabase db = getSQLiteDb(true, context);
                long l = 0;
                l = db.insertWithOnConflict(USER_LOGIN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                db.close();
                if (l > 0)
                    a = true;
            } catch (SQLException se) {
                se.printStackTrace();
            }

            return a;
        }
    }


    protected boolean updateUserData(Context context,LoginModel loginModel) {
        boolean b = false;
        synchronized (Database_Helper.lock) {
            try {
                SQLiteDatabase db = getSQLiteDb(true, context);
                ContentValues values = new ContentValues();
                values.put(USERID, loginModel.getUserId());
                values.put(NAME, loginModel.getName());
                values.put(EMAIL, loginModel.getEmailId());
                values.put(MOBILENUMBER, loginModel.getMobile());
                values.put(DOB, loginModel.getDOB());
                values.put(GENDER, loginModel.getGender());
                values.put(ACCESSTOKEN, loginModel.getAccess_Token());
                values.put(REFRESHTOKEN, loginModel.getRefresh_Token());
                values.put(MANAGERNAME, loginModel.getManagerName());
                values.put(MANAGERID, loginModel.getManagerId());
                values.put(ISMANAGER,  String.valueOf(loginModel.isManager()));
                values.put(OFFICELOCATION, loginModel.getOfficeLocation());
                values.put(ALPHANUMERIC_ENC, loginModel.getAlphaNumeric_Ecn());
                values.put(ALPHANUMERIC_ENC_MANAGER, loginModel.getAlphaNumeric_Ecn_managerid());

                long i = db.update(USER_LOGIN, values, USERID + "=?", new String[]{String.valueOf(loginModel.getUserId())});

                db.close();
                if (i != -1)
                    b = true;

            } catch (SQLException se) {
                se.printStackTrace();
            }
            return b;
        }
    }

    protected LoginModel getLogin(Context context) {
        LoginModel loginModel = null;
        synchronized (Database_Helper.lock) {

            SQLiteDatabase db = getSQLiteDb(false, context);
            try {
                String query = "SELECT * FROM " + USER_LOGIN;
                Cursor cursor = db.rawQuery(query, null);
                if (cursor.moveToFirst())
                    loginModel = cursorData(cursor);

                cursor.close();
                db.close();

            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return loginModel;
    }

    protected boolean signOut(Context context) {
        boolean a = false;
        synchronized (Database_Helper.lock) {

            SQLiteDatabase db = getSQLiteDb(true, context);
            try {
                db.execSQL("DELETE FROM " + USER_LOGIN);
                db.close();
                a = true;
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return a;
    }

    @SuppressLint("Range")
    private LoginModel cursorData(Cursor cursor) {
        LoginModel loginModel = new LoginModel();
        loginModel.setUserId(cursor.getString(cursor.getColumnIndex(USERID)));
        loginModel.setName(cursor.getString(cursor.getColumnIndex(NAME)));
        loginModel.setEmailId(cursor.getString(cursor.getColumnIndex(EMAIL)));
        loginModel.setMobile(cursor.getString(cursor.getColumnIndex(MOBILENUMBER)));
        loginModel.setDOB(cursor.getString(cursor.getColumnIndex(DOB)));
        loginModel.setGender(cursor.getString(cursor.getColumnIndex(GENDER)));
        loginModel.setAccess_Token(cursor.getString(cursor.getColumnIndex(ACCESSTOKEN)));
        loginModel.setRefresh_Token(cursor.getString(cursor.getColumnIndex(REFRESHTOKEN)));
        loginModel.setManagerId(cursor.getInt(cursor.getColumnIndex(MANAGERID)));
        loginModel.setManagerName(cursor.getString(cursor.getColumnIndex(MANAGERNAME)));
        loginModel.setManager(getBooleanValue(cursor.getString(cursor.getColumnIndex(ISMANAGER))));
        loginModel.setOfficeLocation(cursor.getString(cursor.getColumnIndex(OFFICELOCATION)));
        loginModel.setAlphaNumeric_Ecn(cursor.getString(cursor.getColumnIndex(ALPHANUMERIC_ENC)));
        loginModel.setAlphaNumeric_Ecn_managerid(cursor.getString(cursor.getColumnIndex(ALPHANUMERIC_ENC_MANAGER)));

        return loginModel;
    }


    protected boolean updateAccessAndRefreshToken(Context context,String userId, String accessToken,String refreshToken) {
        boolean b = false;
        synchronized (Database_Helper.lock) {
            try {
                SQLiteDatabase db = getSQLiteDb(true, context);
                ContentValues values = new ContentValues();
                values.put(ACCESSTOKEN, accessToken);
                values.put(REFRESHTOKEN, refreshToken);
                long i = db.update(USER_LOGIN, values, USERID + "=?", new String[]{userId});

                db.close();
                if (i != -1)
                    b = true;

            } catch (SQLException se) {
                se.printStackTrace();
            }
            return b;
        }
    }

    private static boolean getBooleanValue(String value) {
        boolean i = false;
        try {
            i = Boolean.parseBoolean(value);
        } catch (Exception e) {
        }
        return i;
    }
}

