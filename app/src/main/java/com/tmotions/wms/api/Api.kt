package com.tmotions.wms.api

import com.google.gson.JsonObject
import com.tmotions.wms.models.*
import com.tmotions.wms.models.dashboardModels.DashboardResponseModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.leaveSummaryDetails.LeaveSummaryDetailsResponse
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummaryResponseModel
import com.tmotions.wms.models.leavedays.LeaveDaysRequestModel
import com.tmotions.wms.models.leavedays.LeaveDaysResponseModel
import com.tmotions.wms.models.managerview.Data
import com.tmotions.wms.models.managerview.ManagerFilterResponseModel
import com.tmotions.wms.models.notificationmodels.NotificationResponseModel
import com.tmotions.wms.models.restrictedholidaymodels.RestrictedHolidayModel
import com.tmotions.wms.models.tokenexpiremodel.TokenExpireModel
import com.tmotions.wms.models.wfhresourcelist.ResourceListResponse
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.http.*

interface Api {

    @POST("Login/SendOTPToEmail")
    fun sendOTPToEmail(@Header("Authorization") token: String, @Body loginModel: LoginModel): Call<SentOtpResponseModel>

    @POST("Login/VerifyOTP")
    fun verifyOTP(@Header("Authorization") token: String, @Body requestOtpVerifyModel: RequestOtpVerifyModel): Call<VerifyOTPResponseModel>

    @POST("Login/refreshtoken")
    fun refreshToken(@Header("Authorization") token: String,@Body loginModel: LoginModel): Call<TokenExpireModel>

    @GET("Dashboard/GetCalenderHolidayAndLeaveList")
    fun getCalenderHolidayAndLeaveList(@Header("Authorization") token: String, @Query("Year") Year: Int, @Query("Month") Month: Int): Call<DashboardResponseModel>

    @GET("ApplyRequest/GetHolidayList")
    fun getHolidayList(@Header("Authorization") token: String): Call<HolidayCalenderModel>

    @GET("ApplyRequest/GetLeaveSummaryFilter")
    fun getLeaveSummaryFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

    @GET("ApplyRequest/GetLeaveSummaryList")
    fun getLeaveSummaryList(@Header("Authorization") token: String, @Query("LeaveType") Year: String, @Query("Status") Month: String, @Query("FromDate") FromDate: String): Call<LeaveSummaryResponseModel>

    @GET("ApplyRequest/GetLeaveSummaryDetail")
    fun getLeaveSummaryDetail(@Header("Authorization") token: String, @Query("LeaveGUID") LeaveGUID: String, @Query("noficationGuid") noficationGuid: String): Call<LeaveSummaryDetailsResponse>

    @GET("ApplyRequest/GetLeaveFilter")
    fun getLeaveFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

    @POST("ApplyRequest/ApplyLeave")
    fun getApplyLeave(@Header("Authorization") token: String,  @Body leaveRequestModel: LeaveRequestModel): Call<LeaveApplyResponseModel>

    @GET("ApplyRequest/GetOverTimeFilter")
    fun getOverTimeFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

//    @GET("ApplyRequest/GetLeaveFilter")
//    fun getLeaveFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

    @POST("ApplyRequest/ApplyOverTime")
    fun getApplyOverTime(@Header("Authorization") token: String,  @Body leaveRequestModel: LeaveRequestModel): Call<LeaveApplyResponseModel>

    @GET("Dashboard/GetHolidayCalenderPdf")
    fun getHolidayCalenderPdf(@Header("Authorization") token: String): Call<ResponseBody>

    @POST("ApplyRequest/ChangeLeaveStatus")
    fun getChangeLeaveStatus(@Header("Authorization") token: String,  @Body leaveRequestModel: LeaveRequestModel): Call<LeaveApplyResponseModel>

    @POST("ApplyRequest/GetLeaveDays")
    fun getLeaveDays(@Header("Authorization") token: String,  @Body leaveDaysRequestModel: LeaveDaysRequestModel): Call<LeaveDaysResponseModel>

    @GET("ApplyRequest/GetRestrictedHolidayList")
    fun getRestrictedHolidayList(@Header("Authorization") token: String): Call<RestrictedHolidayModel>
    /////////////////////// Manager view apis /////////////////////////

    @GET("ApplyRequest/GetManagerLeaveSummaryFilter")
    fun getManagerLeaveSummaryFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

    @GET("ApplyRequest/GetResourceAndLeaveTypeList")
    fun getResourceAndLeaveTypeList(@Header("Authorization") token: String): Call<ManagerFilterResponseModel>

    @GET("ApplyRequest/GetManagerLeaveSummaryList")
    fun getManagerLeaveSummaryList(@Header("Authorization") token: String,@Query("LeaveType") leaveType: String,@Query("employeeId") employeeId: String): Call<LeaveSummaryResponseModel>

    @GET("ApplyRequest/GetLeaveSummaryByDate")
    fun getLeaveSummaryByDate(@Header("Authorization") token: String,@Query("leaveDate") leaveDate: String): Call<LeaveSummaryResponseModel>

    @GET("ApplyRequest/GetManagerTeamUpcomingFilter")
    fun getManagerTeamUpcomingFilter(@Header("Authorization") token: String): Call<FilterResponseModel>

    @GET("ApplyRequest/GetManagerTeamUpcomingLeaveSummary")
    fun getManagerTeamUpcomingLeaveSummary(@Header("Authorization") token: String, @Query("LeaveType") Year: String, @Query("Status") Month: String, @Query("FromDate") FromDate: String,@Query("AlphaNumeric_ECN") employeeId: String): Call<LeaveSummaryResponseModel>

    @GET("ApplyRequest/GetUserNotification")
    fun getNotificationList(@Header("Authorization") token: String): Call<NotificationResponseModel>

    @GET("Login/LogOut")
    fun getUserLogout(@Header("Authorization") token: String): Call<LogoutResponseModel>

    @GET("ApplyRequest/GetResourceList")
    fun getResourceList(@Header("Authorization") token: String): Call<ResourceListResponse>

    @POST("ApplyRequest/GetManagerLeaveDays")
    fun getManagerLeaveDays(@Header("Authorization") token: String,  @Body leaveDaysRequestModel: LeaveDaysRequestModel): Call<LeaveDaysResponseModel>

    @POST("ApplyRequest/ManagerApplyLeave")
    fun getManagerApplyLeave(@Header("Authorization") token: String,  @Body leaveRequestModel: LeaveRequestModel): Call<LeaveApplyResponseModel>

    @POST("ApplyRequest/ChangeLeaveStatusInBulk")
    fun getChangeLeaveStatusInBulk(@Header("Authorization") token: String,  @Body jbsbdhd: List<String>): Call<LeaveApplyResponseModel>

}