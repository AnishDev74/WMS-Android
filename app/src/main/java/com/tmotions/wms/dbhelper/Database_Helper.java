package com.tmotions.wms.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database_Helper extends SQLiteOpenHelper {

    private static final String DB_NAME = "wms.db";
    private static final int DB_VERSION = 3;
    private static Database_Helper databaseHelper;
    private static SQLiteDatabase db = null;
    protected static final Object lock = new Object();
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USERID = "USERID";
    public static final String NAME = "FIRSTNAME";
    public static final String EMAIL = "EMAIL";
    public static final String MOBILENUMBER = "MOBILENUMBER";
    public static final String DOB = "DOB";
    public static final String GENDER = "GENDER";
    public static final String ACCESSTOKEN = "ACCESSTOKEN";
    public static final String REFRESHTOKEN = "REFRESHTOKEN";
    public static final String MANAGERNAME = "MANAGERNAME";
    public static final String MANAGERID = "MANAGERID";
    public static final String ISMANAGER = "ISMANAGER";
    public static final String OFFICELOCATION = "OFFICELOCATION";
    public static final String ALPHANUMERIC_ENC = "ALPHANUMERIC_ENC";
    public static final String ALPHANUMERIC_ENC_MANAGER = "ALPHANUMERIC_ENC_MANAGER";

    public Database_Helper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized Database_Helper getInstance(Context context) {
        synchronized (lock) {
            if (databaseHelper == null) {
                databaseHelper = new Database_Helper(context.getApplicationContext());
            }
        }
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String USER_LOGIN_MST = "CREATE TABLE " + USER_LOGIN + "(" +
                USERID + " INTEGER ," +
                NAME + " TEXT," +
                EMAIL + " TEXT," +
                MOBILENUMBER + " TEXT," +
                DOB + " TEXT," +
                GENDER + " TEXT," +
                ACCESSTOKEN + " TEXT," +
                REFRESHTOKEN + " TEXT," +
                MANAGERNAME + " TEXT," +
                MANAGERID + " INTEGER," +
                ISMANAGER + " TEXT," +
                OFFICELOCATION + " TEXT," +
                ALPHANUMERIC_ENC + " TEXT," +
                ALPHANUMERIC_ENC_MANAGER + " TEXT" +
                ")";

        db.execSQL(USER_LOGIN_MST);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String sql1 = "ALTER TABLE " + USER_LOGIN + " ADD COLUMN " +
                    OFFICELOCATION + " TEXT";
            sqLiteDatabase.execSQL(sql1);
        }
        if(oldVersion < 3){
            String sql2 = "ALTER TABLE " + USER_LOGIN + " ADD COLUMN " +
                        ALPHANUMERIC_ENC + " TEXT";
            String sql3 = "ALTER TABLE " + USER_LOGIN + " ADD COLUMN " +
                        ALPHANUMERIC_ENC_MANAGER + " TEXT";
            sqLiteDatabase.execSQL(sql2);
            sqLiteDatabase.execSQL(sql3);
        }
    }
}
