package com.tmotions.wms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.tmotions.wms.adapters.CalendarAdapter
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.ResourceListDropDownAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.PermissionUtils
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.EmpWfhActivityBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.HolidayModel
import com.tmotions.wms.models.LeaveApplyResponseModel
import com.tmotions.wms.models.LeaveRequestModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leavedays.LeaveDaysRequestModel
import com.tmotions.wms.models.leavedays.LeaveDaysResponseModel
import com.tmotions.wms.models.wfhresourcelist.Data
import com.tmotions.wms.models.wfhresourcelist.ResourceListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class EmpWFHActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    lateinit var binding: EmpWfhActivityBinding
    var startDate = ""
    var endDate = ""
    var month: GregorianCalendar? = null
    var itemmonth: GregorianCalendar? = null
    val arrlist_calendar = ArrayList<HolidayModel>()
    private var sessionFromList = ArrayList<LeaveList>()
    private var sessionToList = ArrayList<LeaveList>()
    private var managerList = ArrayList<LeaveList>()
    private var resourceList = ArrayList<Data>()
    var managerEmail = ""
    var sessonFromValue = ""
    var sessionToValue = ""
    var managerValue = ""
    private var applyForNameValue = ""
    private var applyForEmailValue = ""
    private var applyForIdValue = 0
    private var isSpinner1InitialCheck = true
    private var isSpinner2InitialCheck = true
    var snackbar: Snackbar? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
    var userCountry = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.emp_wfh_activity)
        binding.headerLayout.txtTitle.text = "Employee WFH"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }
        MyApplication.instance.activity = this
        Utility.sendViewScreenEvent("EmpWfh_Activity_Android",this)

        month = GregorianCalendar.getInstance() as GregorianCalendar
        itemmonth = month!!.clone() as GregorianCalendar
        var adapter = CalendarAdapter(this, month, arrlist_calendar)

        binding.spinnerFromSession.onItemSelectedListener = this
        binding.spinnerToSession.onItemSelectedListener = this
        binding.spinnerManager.onItemSelectedListener = this
        binding.spinnerApplyingFor.onItemSelectedListener = this

        Utility.handleMultilineEditText(binding.edtReason)
        binding.edtReason.setFilters(arrayOf(Utility.ignoreFirstWhiteSpace()))

        binding.txtFromDate.text = Utility.getCurrentDate()
        binding.txtToDate.text = Utility.getCurrentDate()
        startDate = Utility.getCurrentDateSend()
        endDate = Utility.getCurrentDateSend()

        binding.imgFromdate.setOnClickListener(this)
        binding.imgTodate.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnApply.setOnClickListener(this)
        binding.headerLayout.imgBack.setOnClickListener(this)

        if(Utility.isOnline(this)) {
            getLeaveFilter()
            getResourceList()
        }
        else
            Utility.displayAlertDialog(this@EmpWFHActivity, resources.getString(R.string.nointernet),
                layoutInflater)

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgFromdate -> {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    this,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        binding.txtFromDate.setText(
                            Utility.displayDateFormat(
                                dayOfMonth,
                                monthOfYear,
                                year
                            )
                        )
                        startDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        getManagerLeaveDays()
                    },
                    year,
                    month,
                    day
                )

                dpd.show()
            }
            R.id.imgTodate -> {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    this,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        binding.txtToDate.setText(
                            Utility.displayDateFormat(
                                dayOfMonth,
                                monthOfYear,
                                year
                            )
                        )
                        endDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        getManagerLeaveDays()
                     },
                    year,
                    month,
                    day
                )
                dpd.show()
            }
            R.id.btnApply ->{
                if (applyForNameValue.equals("Select Employee")) {
                    Utility.displayAlertDialog(this,"Please select Employee",layoutInflater)
                }
                else if (userCountry.isNullOrEmpty()) {
                    Utility.displayAlertDialog(this,"User location not found ",layoutInflater)
                }
                else {
                    if (Utility.isOnline(this)) {
                        Utility.sendActionEvent("EmpWfhApply_Button_Android",this)
                        getManagerApplyLeave()
                    } else {
                        Utility.displayAlertDialog(this,resources.getString(R.string.nointernet),layoutInflater)
                    }
                }
            }
            R.id.btnCancel ->{
                Utility.sendActionEvent("EmpWfhCancel_Button_Android",this)
                finish()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }

    private fun getLeaveFilter() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@EmpWFHActivity)
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.shimmerFrameLayout.startShimmer()

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<FilterResponseModel> = retrofitApiInterface.getLeaveFilter(loginModel.access_Token)

        mService.enqueue(object : Callback<FilterResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<FilterResponseModel>, response: Response<FilterResponseModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            binding.shimmerFrameLayout.stopShimmer()
                            val data = filterResponseModel!!.data

                            sessionFromList = data.sessionList
                            sessionToList = data.sessionList
                            managerList = data.managerList
                            if(!data.managerEmail.isNullOrEmpty())
                                managerEmail = data.managerEmail

                            if(sessionFromList.size > 0)
                                setSessionFromList(sessionFromList)

                            if(sessionToList.size > 0)
                                setSessionToList(sessionToList)

                            if(managerList.size > 0)
                                setManagerList(managerList)

//                          binding.spinTransaction.onItemSelectedListener = this@EmpWFHActivity
//                          binding.spinnerFromSession.onItemSelectedListener = this@EmpWFHActivity
//                          binding.spinnerToSession.onItemSelectedListener = this@EmpWFHActivity
//                          binding.spinnerManager.onItemSelectedListener = this@EmpWFHActivity

                            binding.loutRoot.visibility = View.VISIBLE

                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@EmpWFHActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveFilter()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                            binding.shimmerFrameLayout.stopShimmer()
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                binding.shimmerFrameLayout.stopShimmer()
                                Utility.displayAlertDialog(
                                    this@EmpWFHActivity,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        binding.shimmerFrameLayout.stopShimmer()
                        Utility.displayAlertDialog(
                            this@EmpWFHActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<FilterResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@EmpWFHActivity, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
                binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    private fun getResourceList() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@EmpWFHActivity)
//        binding.shimmerFrameLayout.visibility = View.VISIBLE
//        binding.shimmerFrameLayout.startShimmer()

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<ResourceListResponse> = retrofitApiInterface.getResourceList(loginModel.access_Token)

        mService.enqueue(object : Callback<ResourceListResponse> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<ResourceListResponse>, response: Response<ResourceListResponse>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
//                            binding.shimmerFrameLayout.visibility = View.GONE
//                            binding.shimmerFrameLayout.stopShimmer()
                            resourceList.addAll(filterResponseModel!!.data)

                            setResourceList(resourceList)
//                            val data = filterResponseModel!!.data
//                            resourceList = data
//                            if(resourceList.size > 0) {
//                                setResourceList(resourceList)
//                            }
                         } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@EmpWFHActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getResourceList()
                                        }
                                        override fun onFailure() {
//                                            binding.shimmerFrameLayout.visibility = View.GONE
//                                            binding.shimmerFrameLayout.stopShimmer()
                                        }
                                    })
                            else {
//                                binding.shimmerFrameLayout.visibility = View.GONE
//                                binding.shimmerFrameLayout.stopShimmer()
                                Utility.displayAlertDialog(
                                    this@EmpWFHActivity,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
//                        binding.shimmerFrameLayout.visibility = View.GONE
//                        binding.shimmerFrameLayout.stopShimmer()
                        Utility.displayAlertDialog(
                            this@EmpWFHActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResourceListResponse>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@EmpWFHActivity, "$t", layoutInflater)
//                binding.shimmerFrameLayout.visibility = View.GONE
//                binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    private fun setSessionFromList(statusList: ArrayList<LeaveList>) {

        val dropDownAdapter = DropDownAdapter(
            this@EmpWFHActivity,
            statusList
        )
        binding.spinnerFromSession.adapter = dropDownAdapter
    }

    private fun setSessionToList(statusList: ArrayList<LeaveList>) {

        val dropDownAdapter = DropDownAdapter(
            this@EmpWFHActivity,
            statusList
        )
        binding.spinnerToSession.adapter = dropDownAdapter
        binding.spinnerToSession.setSelection(1)
        sessionToValue = "Session 2"
    }

    private fun setManagerList(statusList: ArrayList<LeaveList>) {

        val dropDownAdapter = DropDownAdapter(
            this@EmpWFHActivity,
            statusList
        )
        binding.spinnerManager.adapter = dropDownAdapter
    }

    private fun setResourceList(resourceList: ArrayList<Data>) {

        val dropDownAdapter = ResourceListDropDownAdapter(
            this@EmpWFHActivity,
            resourceList
        )
        binding.spinnerApplyingFor.adapter = dropDownAdapter
    }

    override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, p3: Long) {

        when (parent.id) {
            R.id.spinnerFromSession ->{
                val statusModel = sessionFromList.get(position)
                sessonFromValue = statusModel.Text
                if (isSpinner1InitialCheck) {
                    isSpinner1InitialCheck = false;
                } else {
                    getManagerLeaveDays()
                }
            }
            R.id.spinnerToSession ->{
                val statusModel = sessionToList.get(position)
                sessionToValue = statusModel.Text
                if (isSpinner2InitialCheck) {
                    isSpinner2InitialCheck = false;
                } else {
                    getManagerLeaveDays()
                }
            }
            R.id.spinnerManager ->{
                val statusModel = managerList.get(position)
                if(!statusModel.Text.equals("Select Manager"))
                    managerValue = statusModel.Text
                else
                    managerValue = ""
            }
            R.id.spinnerApplyingFor ->{
                val statusModel = resourceList.get(position)
                applyForNameValue = statusModel.tm_employee_name
                if(!statusModel.tm_employee_name.equals("Select Employee")) {
                    applyForNameValue = statusModel.tm_employee_name
                    applyForIdValue = statusModel.tm_employeeid
                    applyForEmailValue = statusModel.tm_employeeemail
                }
                else {
                    applyForNameValue = statusModel.tm_employee_name
                    applyForIdValue = 0
                    applyForEmailValue = ""
                }
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun getManagerLeaveDays() {
        Utility.setWindowNotClickable(window)
        binding.mprogress.visibility = View.VISIBLE
        var leaveDaysRequestModel = LeaveDaysRequestModel()

        leaveDaysRequestModel.leaveType = "WFH"
        leaveDaysRequestModel.startDate = startDate+Utility.getcurrentTime()
        leaveDaysRequestModel.endDate = endDate+Utility.getcurrentTime()
        leaveDaysRequestModel.sessionStart = sessonFromValue
        leaveDaysRequestModel.sessionEnd = sessionToValue
        leaveDaysRequestModel.isOverTime = false
        leaveDaysRequestModel.timeZone = userCountry

        val loginModel = Login_Helper.getLogin(this@EmpWFHActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveDaysResponseModel> = retrofitApiInterface.getManagerLeaveDays(loginModel.access_Token,leaveDaysRequestModel)

        mService.enqueue(object : Callback<LeaveDaysResponseModel> {
            override fun onResponse(call: Call<LeaveDaysResponseModel>, response: Response<LeaveDaysResponseModel>) {
                /* binding.mprogress.visibility = View.GONE*/
                Utility.setWindowClickable(window)
                binding.spinnerToSession.onItemSelectedListener = this@EmpWFHActivity
                binding.spinnerFromSession.onItemSelectedListener = this@EmpWFHActivity
                try {
                    if (response.isSuccessful) {
                        val leaveDaysResponseModel = response.body()
                        if (leaveDaysResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            val leaveDaysResponseDataModel = leaveDaysResponseModel.data
                            binding.txtApplyingFor.text = "Applying For : "+leaveDaysResponseDataModel.Days.toString()
                        }
                        else {
                            if (leaveDaysResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@EmpWFHActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getManagerLeaveDays()
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@EmpWFHActivity,
                                    leaveDaysResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@EmpWFHActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveDaysResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@EmpWFHActivity, "$t", layoutInflater)
                binding.spinnerToSession.onItemSelectedListener = this@EmpWFHActivity
                binding.spinnerFromSession.onItemSelectedListener = this@EmpWFHActivity
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun getManagerApplyLeave() {
        Utility.setWindowNotClickable(window)
        var loginModl = Login_Helper.getLogin(this)
        binding.btnApply.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.leaveType = "WFH"
        leaveRequestModel.startDate = startDate+Utility.getcurrentTime()
        leaveRequestModel.endDate = endDate+Utility.getcurrentTime()
        leaveRequestModel.sessionStart = sessonFromValue
        leaveRequestModel.sessionEnd = sessionToValue
        leaveRequestModel.applyForEmail = applyForEmailValue
        leaveRequestModel.applyForId = applyForIdValue
        leaveRequestModel.applyForName = applyForNameValue
        leaveRequestModel.ccManagerEmail = managerValue
      //  leaveRequestModel.managerEmail = managerEmail
        leaveRequestModel.managerEmail = loginModl.emailId
        leaveRequestModel.remarks = binding.edtReason.text.toString()
        leaveRequestModel.timeZone = userCountry

        val loginModel = Login_Helper.getLogin(this@EmpWFHActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getManagerApplyLeave(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(window)
                binding.btnApply.isEnabled = true
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            Toast.makeText(this@EmpWFHActivity,baseResponseModel.message, Toast.LENGTH_LONG).show()
//                            val intent = Intent(this@EmpWFHActivity, ManagerViewActivity::class.java)
//                            startActivity(intent)
                            finish()
                        }
                        else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@EmpWFHActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getManagerApplyLeave()
                                        }
                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@EmpWFHActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@EmpWFHActivity,
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
                Utility.displayAlertDialog(this@EmpWFHActivity, "$t", layoutInflater)
                binding.btnApply.isEnabled = true
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    // location implementation start

    override fun onStart() {
        super.onStart()
        if(!MyApplication.instance.getUserLocation().isNullOrEmpty()) {
            userCountry = MyApplication.instance.getUserLocation()
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
                                this@EmpWFHActivity,
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

    override fun onRequestPermissionsResult(
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
            val addresses: List<Address>? = geocoder.getFromLocation(lat,lng, 1)
            if(!addresses.isNullOrEmpty())
                userCountry = "" + addresses!![0].countryName
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addressForAPI33AndAbove =
                    geocoder.getFromLocation(lat, lng, 1) { p0 ->
                        val address = p0[0]
                        if (address != null)
                            userCountry = address.countryName
                    }
            }
            else {
                val address = geocoder.getFromLocation(lat, lng, 1)?.get(0)
                if (address != null)
                    userCountry = address.countryName
            }*/

            if(!userCountry.isNullOrEmpty()) {
                MyApplication.instance.setUserLocation(userCountry)
            }

            if(snackbar!=null)
            snackbar!!.dismiss()

        } catch (e: IOException) {
            // Utils.log("Check internet: ${e.message}")
        }
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)/*.setNumUpdates(1)*/
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
    }

}
