package com.tmotions.wms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.tmotions.wms.R
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.PermissionUtils
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityManagerLeaveSummaryDetailsBinding

import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LeaveApplyResponseModel
import com.tmotions.wms.models.LeaveRequestModel
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.leaveSummaryDetails.Data
import com.tmotions.wms.models.leaveSummaryDetails.LeaveSummaryDetailsResponse
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class ManagerLeaveSummaryDetailsActivity : AppCompatActivity(),View.OnClickListener {

    var guid = ""
    private var noficationGuid = ""
    var status = ""
    lateinit var binding: ActivityManagerLeaveSummaryDetailsBinding
    lateinit var loginmodel: LoginModel

 //   var snackbar: Snackbar? = null
  /*  private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
    var userCountry = ""*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_leave_summary_details)

        binding.headerLayout.txtTitle.text = "Leave Details"

        Utility.setActionBar(this, window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                false
        }

        MyApplication.instance.activity = this
        Utility.sendViewScreenEvent("ManagerLeaveSummaryDetails_Activity_Android",this)

        loginmodel = Login_Helper.getLogin(this@ManagerLeaveSummaryDetailsActivity)
        guid = intent.getStringExtra("guid").toString()
        noficationGuid = intent.getStringExtra("noficationGuid").toString()

        binding.btnAccept.setOnClickListener(this)
        binding.btnDecline.setOnClickListener(this)
        binding.headerLayout.imgBack.setOnClickListener(this)

        if(Utility.isOnline(this)) {
            getLeaveSummaryDetail()
        }
        else
            Utility.displayAlertDialog(
                this@ManagerLeaveSummaryDetailsActivity,
                resources.getString(R.string.nointernet),
                layoutInflater
            )

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getLeaveSummaryDetail() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@ManagerLeaveSummaryDetailsActivity)
        binding.shimmerFrameLayout.visibility = View.VISIBLE

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<LeaveSummaryDetailsResponse> =
            retrofitApiInterface.getLeaveSummaryDetail(loginModel.access_Token, guid,noficationGuid)

        mService.enqueue(object : Callback<LeaveSummaryDetailsResponse> {
            override fun onResponse(
                call: Call<LeaveSummaryDetailsResponse>,
                response: Response<LeaveSummaryDetailsResponse>
            ) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            val data = holidayCalenderModel!!.data
                            setData(data)
                            binding.loutRoot.visibility = View.VISIBLE

                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerLeaveSummaryDetailsActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveSummaryDetail()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ManagerLeaveSummaryDetailsActivity,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    } else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ManagerLeaveSummaryDetailsActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveSummaryDetailsResponse>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(
                    this@ManagerLeaveSummaryDetailsActivity,
                    "$t",
                    layoutInflater
                )
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setData(data: Data) {

        binding.txtAppliedOn.text = data.appliedOn
//        binding.txtAppliedOn.text = Utility.getDateFromUtc(data.appliedOn)//getDateByUserTimeZone
        binding.txtLeaveType.text = data.leaveType
        binding.txtCategory.text = data.leaveCategory
        binding.txtFromdate.text = data.fromDate +" -" + data.toDate
        binding.txtFromsession.text = data.fromSession +" - "+ data.toSession
//        binding.txtTodate.text = data.toDate
//        binding.txtTosession.text = data.toSession
        if(!data.reason.isNullOrEmpty())
        binding.txtReason.text = data.reason
        status = data.requestStatus

        var str = data.noOfDays.toString().split(".")
        if (str[1].equals("0"))
            binding.txtNumDays.text = str[0]
        else
            binding.txtNumDays.text = data.noOfDays.toString()

        binding.txtStatus.text = status
        binding.txtPendingWith.text = data.approverName

        if(data.leaveCategory.equals("Leave Cancel") && ((status.equals("Approved") || status.equals("Rejected")))){
            binding.loutButtons.visibility = View.GONE
        }

        if(status.equals("Pending")){
            binding.lblPendingWith.text = "Pending With"
            binding.loutButtons.visibility = View.VISIBLE
        }else if(status.equals("Approved")){
            binding.lblPendingWith.text = "Approved By"
            binding.loutButtons.visibility = View.GONE
        }else if(status.equals("Withdrawn")){
            binding.lblPendingWith.text = "Withdrawn By"
         //   binding.txtPendingWith.text = loginmodel.name.toString()//need to change
            binding.loutButtons.visibility = View.GONE
        }else if(status.equals("Disapproved")){
            binding.lblPendingWith.text = "Disapproved By"
            binding.loutButtons.visibility = View.GONE
        }else if(status.equals("Rejected")){
            binding.lblPendingWith.text = "Rejected By"
            binding.loutButtons.visibility = View.GONE
        }else if(status.equals("Cancelled")){
            binding.lblPendingWith.text = "Pending With"
            binding.txtPendingWith.text = loginmodel.name.toString()
            binding.loutButtons.visibility = View.VISIBLE
        }
        /////////////// set leave data /////////////////
        var el = data.EL
        if(el != null){
            if(el.leaveTypeID != 0) {
                binding.earnedLeaveTxtView.text = "Earned Leave ("+el.balance+")"
                binding.txtGranted.text = "Granted ("+el.granted+")"
                binding.txtAvailed.text = "Availed ("+el.availed+")"
                binding.txtCarryForward.text = "Carry Forwward ("+el.carryForwarded+")"

            }
        }
        var cl = data.CL
        if(cl != null){
            if(cl.leaveTypeID != 0) {
                binding.casualLeaveTxtView.text = "Casual Leave ("+cl.balance+")"
                binding.clAvailedTxtView.text =  "Availed ("+cl.availed+")"
                binding.clGrantedTxtView.text = "Granted ("+cl.granted+")"
            }
        }
        var sl = data.SL
        if(sl != null){
            if(sl.leaveTypeID != 0) {
                binding.sickLeaveTxtView.text = "Sick Leave ("+sl.balance+")"
                binding.slAvailedTxtView.text =  "Availed ("+sl.availed+")"
                binding.slGrantedTxtView.text = "Granted ("+sl.granted+")"
            }
        }
        var co = data.CO
        if(co != null){
            if(co.leaveTypeID != 0) {
                binding.compOffTxtView.text = "Comp-Off ("+co.balance+")"
                binding.coAvailedTxtView.text =  "Availed ("+co.availed+")"
                binding.coGrantedTxtView.text = "Granted ("+co.granted+")"
            }
        }
        var rh = data.RH
        if(rh != null){
            if(rh.leaveTypeID != 0) {
                binding.rHLeaveTxtView.text = "Restricted Holiday ("+rh.balance+")"
                binding.rHAvailedTxtView.text = "Availed ("+rh.availed+")"
                binding.rHGrantedTxtView.text = "Granted ("+rh.granted+")"
            }
        }
///////////////////////////////////// set data according office location /////////////////////////////////

        if (data.OfficeLocation.isNullOrEmpty()){
            binding.leaveBalanceLayout.visibility = View.GONE
        }
        else{
            if(data.OfficeLocation == "UK")
            {
                binding.rHLeaveLinearLayout.visibility =      View.GONE
                binding.earnedLeaveLinearLayout.visibility =  View.GONE
                binding.compOffLeaveLinearLayout.visibility = View.GONE

            }
            else{
                binding.leaveBalanceLayout.visibility = View.VISIBLE
                binding.rHLeaveLinearLayout.visibility =      View.VISIBLE
                binding.earnedLeaveLinearLayout.visibility =  View.VISIBLE
                binding.compOffLeaveLinearLayout.visibility = View.VISIBLE
                binding.sickLeaveLinearLayout.visibility =    View.VISIBLE
                binding.casualLeaveLinearLayout.visibility =  View.VISIBLE
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAccept ->{
             /*   if (userCountry.isNullOrEmpty()) {
                    Utility.displayAlertDialog(this,"User location not found ",layoutInflater)
                }
                else{
                    if (Utility.isOnline(this)) {s
                        Utility.sendActionEvent("ManagerLeaveSummaryApproved_Button_Android",this)

                        getChangeLeaveStatus(
                            "Approved", ""
                        )
                    } else
                        Utility.displayAlertDialog(
                            this,
                            resources.getString(R.string.nointernet),
                            layoutInflater
                        )
                }*/

                if (Utility.isOnline(this)) {
                    Utility.sendActionEvent("ManagerLeaveSummaryApproved_Button_Android",this)
                    getChangeLeaveStatus(
                        "Approved", "",true
                    )
                } else
                    Utility.displayAlertDialog(
                        this,
                        resources.getString(R.string.nointernet),
                        layoutInflater
                    )
            }

            R.id.btnDecline -> {
                Utility.sendActionEvent("ManagerLeaveSummaryCancel_Button_Android",this)
                reasonDialog()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }

    private fun getChangeLeaveStatus(leaveStatus: String, reason: String,isApproved:Boolean) {
        Utility.setWindowNotClickable(window)
        binding.btnAccept.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.guid = guid
        leaveRequestModel.status = leaveStatus
        leaveRequestModel.reason = reason
     //   leaveRequestModel.timeZone = userCountry

        val loginModel = Login_Helper.getLogin(this@ManagerLeaveSummaryDetailsActivity)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<LeaveApplyResponseModel> =
            retrofitApiInterface.getChangeLeaveStatus(loginModel.access_Token, leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(
                call: Call<LeaveApplyResponseModel>,
                response: Response<LeaveApplyResponseModel>
            ) {
                Utility.setWindowClickable(window)
                binding.btnAccept.isEnabled = true
                 try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            if(isApproved)
                                Utility.playBeep(this@ManagerLeaveSummaryDetailsActivity)
                            Toast.makeText(
                                this@ManagerLeaveSummaryDetailsActivity, baseResponseModel.message,
                                Toast.LENGTH_LONG
                            ).show()
                             val intent = Intent(
                                this@ManagerLeaveSummaryDetailsActivity,
                                TeamRequest::class.java
                            )
                            startActivity(intent)
                            finish()

                        } else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerLeaveSummaryDetailsActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getChangeLeaveStatus(leaveStatus,reason, isApproved)
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ManagerLeaveSummaryDetailsActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    } else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ManagerLeaveSummaryDetailsActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveApplyResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(
                    this@ManagerLeaveSummaryDetailsActivity,
                    "$t",
                    layoutInflater
                )
                binding.btnAccept.isEnabled = true
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun reasonDialog() {
        val alertDialog = AlertDialog.Builder(this)
        val alert: AlertDialog = alertDialog.create()
        val convertView = layoutInflater.inflate(R.layout.reason_dialog_layout, null) as View
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.setCancelable(false)
        alert.setView(convertView)
        val edtReason = convertView.findViewById<EditText>(R.id.edtReason)
        val btnCancel = convertView.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = convertView.findViewById<Button>(R.id.btnSubmit)
        val textErrorReason = convertView.findViewById<TextView>(R.id.textErrorReason)

        Utility.handleMultilineEditText(edtReason)
        edtReason.setFilters(arrayOf(Utility.ignoreFirstWhiteSpace()))

        btnSubmit.setOnClickListener {

            if (edtReason.text.toString().trim().isEmpty()) {
                textErrorReason.visibility = View.VISIBLE
            } else {
                if (Utility.isOnline(this)) {
                    alert.cancel()
                    textErrorReason.visibility = View.GONE
                    binding.mprogress.visibility = View.VISIBLE
                    if(status.equals("Pending"))
                    getChangeLeaveStatus("Disapproved", edtReason.text.toString(),false)
                    else
                    getChangeLeaveStatus("Rejected", edtReason.text.toString(),false)
                } else
                    Toasty.error(
                        this@ManagerLeaveSummaryDetailsActivity,
                        resources.getString(R.string.please_check_your_internet_connectivity_and_try_again)
                    ).show()
            }

        }
        btnCancel.setOnClickListener {
            alert.cancel()

        }

        alert.show()
    }

    // location implementation start
   /* override fun onStart() {
        super.onStart()
        if(Utility.getBooleanPreference("isLocation") == true) {
            userCountry = Utility.getStringPreference("currentLocation").toString()
        }
        else {
            when {
                PermissionUtils.isAccessFineLocationGranted(this) -> {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            snackbar = Utility.showLocationSnackbar(
                                this,
                                layoutInflater,
                                findViewById<View>(android.R.id.content)
                            )
                            setUpLocationListener()
                        }

                        else -> {
//                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            PermissionUtils.showGPSNotEnabledDialog(
                                this@ManagerLeaveSummaryDetailsActivity,
                                "Location service is disabled in your device. Do you want to enable it?",
                                false
                            )
                        }
                    }
                }

                else -> {
                    if (Utility.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                        PermissionUtils.showLocationEnabledFromSettingDialog(this, false)
                    else {
                        PermissionUtils.requestAccessFineLocationPermission(
                            this,
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }
*/
  /*  override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            //   PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    //   PermissionUtils.showLocationEnabledFromSettingDialog(this,layoutInflater)
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getAddressFromCoordinates(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(MyApplication.instance, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addressForAPI33AndAbove =
                    geocoder.getFromLocation(lat, lng, 1) { p0 ->
                        val address = p0[0]
                        if (address != null)
                            userCountry = address.countryName
                    }
            } else {
                val address = geocoder.getFromLocation(lat, lng, 1)?.get(0)
                if (address != null)
                    userCountry = address.countryName
            }

            if(!userCountry.isNullOrEmpty()) {
                Utility.saveBooleanPreference("isLocation", true)
                Utility.saveStringPreference("currentLocation", userCountry)
            }

            if(snackbar!=null)
            snackbar!!.dismiss()

        } catch (e: IOException) {
            // Utils.log("Check internet: ${e.message}")
        }
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)*//*.setNumUpdates(1)*//*
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var lastLocation: Location? = null
                for (location in locationResult.locations) {
                    if (lastLocation != null) {
                        if (location.time < lastLocation.time) {
                            lastLocation = location
                        }
                    } else {
                        lastLocation = location
                    }
                }
                if (lastLocation != null) {
                    getAddressFromCoordinates(lastLocation.latitude, lastLocation.longitude)
                }
                stopLocationUpdates()
            }
        }
        try {
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        }
        fusedLocationProviderClient = null
    }*/
}
