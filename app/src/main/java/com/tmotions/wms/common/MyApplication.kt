package com.tmotions.wms.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    var mRetrofit: Retrofit? = null
    lateinit var activity: AppCompatActivity
    override fun onCreate() {
        super.onCreate()
        instance = this
       // createNotificationChannel()
    }

    val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .build()

    val retrofitInstance: Retrofit?
        get() {
            val gson = GsonBuilder()
                    .setLenient()
                    .create()
            if (mRetrofit == null) {
                mRetrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(okHttpClient)
                        .build()
            }
            return mRetrofit
        }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Example Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        val TAG = MyApplication::class.java.simpleName
        var mRetrofitDynamic: Retrofit? = null
        private var userLocation = ""
        private var leaveList = ArrayList<LeaveList>()
        private var leaveSummaryTypeList = ArrayList<LeaveList>()
        private var leaveSummaryStatusList = ArrayList<LeaveList>()
        private var overTimeLeaveList = ArrayList<LeaveList>()
        private var managerList = ArrayList<LeaveList>()
        private var managerListShortLeave = ArrayList<LeaveList>()
        private var sessionToList = ArrayList<LeaveList>()
        private var sessionFromList = ArrayList<LeaveList>()
        private var leaveSummaryList = ArrayList<LeaveSummary>()
        private var shortLeaveBalance = 0
        private var managerEmail = ""
        private var applyingTo = ""
        const val CHANNEL_ID = "exampleServiceChannel"

        lateinit var instance: MyApplication


        fun getinstance(): MyApplication? {
            return instance
        }

        fun setinstance(instance: MyApplication) {
            Companion.instance = instance
        }




        //local API Url...
        const val BASE_URL = Constants.BASE_URL

    }
    fun setActity(activity: AppCompatActivity) {
        this.activity = activity
    }

    fun getactivity(): AppCompatActivity {
        return this.activity
    }


    fun getUserLocation(): String {
        return Companion.userLocation
    }

    fun setUserLocation(s: String?) {
        Companion.userLocation = s.toString()
    }

    fun getManagerList(): ArrayList<LeaveList> {
        return Companion.managerList
    }

    fun setMannagerList(s: ArrayList<LeaveList>) {
        Companion.managerList = s
    }
   fun getManagerListShortLeave(): ArrayList<LeaveList> {
        return Companion.managerListShortLeave
    }

    fun setManagerListShortLeave(s: ArrayList<LeaveList>) {
        Companion.managerListShortLeave = s
    }

    fun getLeaveList(): ArrayList<LeaveList> {
        return Companion.leaveList
    }

    fun setLeaveList(s: ArrayList<LeaveList>) {
        Companion.leaveList = s
    }

    fun getSessionToList(): ArrayList<LeaveList> {
        return Companion.sessionToList
    }

    fun setSessionToList(s: ArrayList<LeaveList>) {
        Companion.sessionToList = s
    }

    fun getSessionFromList(): ArrayList<LeaveList> {
        return Companion.sessionFromList
    }

    fun setSessionFromList(s: ArrayList<LeaveList>) {
        Companion.sessionFromList = s
    }
    fun getShortLeaveBalance(): Int {
        return Companion.shortLeaveBalance
    }

    fun setShortLeaveBalance(s: Int) {
        Companion.shortLeaveBalance = s
    }
    fun getApplyingTo(): String {
        return Companion.applyingTo
    }

    fun setApplyingTo(s: String) {
        Companion.applyingTo = s
    }
    fun getOverTimeLeaveList(): ArrayList<LeaveList> {
        return Companion.overTimeLeaveList
    }

    fun setOverTimeLeaveList(s: ArrayList<LeaveList>) {
        Companion.overTimeLeaveList = s
    }
    fun getLeaveSummaryTypeList(): ArrayList<LeaveList> {
        return Companion.leaveSummaryTypeList
    }

    fun setLeaveSummaryTypeList(s: ArrayList<LeaveList>) {
        Companion.leaveSummaryTypeList = s
    }

    fun getLeaveSummaryStatusList(): ArrayList<LeaveList> {
        return Companion.leaveSummaryStatusList
    }

    fun setLeaveSummaryStatusList(s: ArrayList<LeaveList>) {
        Companion.leaveSummaryStatusList = s
    }

    fun getLeaveSummaryList(): ArrayList<LeaveSummary> {
        return Companion.leaveSummaryList
    }

    fun setLeaveSummaryList(s: ArrayList<LeaveSummary>) {
        Companion.leaveSummaryList = s
    }

    fun getManagerEmail(): String {
        return Companion.managerEmail
    }

    fun setManagerEmail(s: String) {
        Companion.managerEmail = s
    }


}