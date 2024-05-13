package com.tmotions.wms.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
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
import com.tmotions.wms.activities.HomeActivity
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.PermissionUtils
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.FragmentApplyOvertimeBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LeaveApplyResponseModel
import com.tmotions.wms.models.LeaveRequestModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leavedays.LeaveDaysRequestModel
import com.tmotions.wms.models.leavedays.LeaveDaysResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.ParseException
import java.util.*

class ApplyOvertimeFragment : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentApplyOvertimeBinding
    var mActivity: FragmentActivity? = null
    private var leaveList = ArrayList<LeaveList>()
    private var sessionFromList = ArrayList<LeaveList>()
    private var sessionToList = ArrayList<LeaveList>()
    private var managerList = ArrayList<LeaveList>()

    var startDate = ""
    var endDate = ""
    var typeValue = ""
    var managerValue = ""
    var managerEmail = ""
    var snackbar: Snackbar? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
    var userCountry = ""
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apply_overtime, container, false)

        MyApplication.instance.activity = mActivity!! as AppCompatActivity
        Utility.sendViewScreenEvent("ApplyOvertime_Fragment_Android",mActivity!!)

        Utility.handleMultilineEditText(binding.edtReason)
        binding.edtReason.filters = arrayOf(Utility.ignoreFirstWhiteSpace())

        binding.txtFromdate.text = Utility.getCurrentDate()
        binding.txtTodate.text = Utility.getCurrentDate()
        startDate = Utility.getCurrentDateSend()
        endDate = Utility.getCurrentDateSend()

        binding.imgFromdate.setOnClickListener(this)
        binding.imgTodate.setOnClickListener(this)
        binding.btnApply.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.txtFromSession.setOnClickListener(this)
        binding.txtToSession.setOnClickListener(this)

        if(MyApplication.instance.getOverTimeLeaveList().size > 0){
            leaveList = MyApplication.instance.getOverTimeLeaveList()
            sessionFromList = MyApplication.instance.getSessionFromList()
            sessionToList = MyApplication.instance.getSessionToList()
            managerList = MyApplication.instance.getManagerList()
            managerEmail =  MyApplication.instance.getManagerEmail()
            setTypeList(leaveList)
            setManagerList(managerList)
            binding.shimmerFrameLayout.visibility = View.GONE
            binding.shimmerFrameLayout.stopShimmer()
            binding.loutRoot.visibility = View.VISIBLE
            binding.txtApplyingTo.text = MyApplication.instance.getApplyingTo()

            binding.spinnerTransactionType.onItemSelectedListener = this@ApplyOvertimeFragment
            binding.spinnerManager.onItemSelectedListener = this@ApplyOvertimeFragment
        }
        else
        getOverTimeFilter()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgFromdate -> {
                Utility.sendActionEvent("ApplyOvertimeFragmentFromDate_Button_Android",mActivity!!)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    mActivity!!,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        binding.txtFromdate.setText(
                            Utility.displayDateFormat(
                                dayOfMonth,
                                monthOfYear,
                                year
                            )
                        )
                        startDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        if (!typeValue.equals("Select Transaction Type")) {
                            try {
                                getLeaveDays()
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }
                    },
                    year,
                    month,
                    day
                )

                dpd.show()
            }
            R.id.imgTodate -> {
                Utility.sendActionEvent("ApplyOvertimeFragmentToDate_Button_Android",mActivity!!)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    mActivity!!,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                        binding.txtTodate.setText(
                            Utility.displayDateFormat(
                                dayOfMonth,
                                monthOfYear,
                                year
                            )
                        )
                        endDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        if (!typeValue.equals("Select Transaction Type")) {
                            try {
                                getLeaveDays()
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }
                    },
                    year,
                    month,
                    day
                )
                dpd.show()
            }
            R.id.txtFromSession -> {
                showFromSessionPopup(view)
            }
            R.id.txtToSession -> {
                showToSessionPopup(view)
            }
            R.id.btnApply ->{

             //   Utility.displayAlertDialog(mActivity!!,"location = > "+userCountry,layoutInflater)

                if (userCountry.isNullOrEmpty()) {
                    Utility.displayAlertDialog(mActivity!!,"User location not found ",layoutInflater)
                }
                else{
                    if (typeValue.equals("Select Transaction Type")) {
                        Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.please_choose_a_transaction_type),layoutInflater)
                    }else if (binding.edtReason.text                            .toString().isNullOrEmpty()) {
                        Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.please_provide_a_reason),layoutInflater)
                    }
                    else {
                        if (Utility.isOnline(mActivity!!)) {
                            Utility.sendActionEvent("ApplyOvertimeFragmentApply_Button_Android",mActivity!!)
                            getApplyOverTime()
                        } else {
                            Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.nointernet),layoutInflater)
                        }
                    }
                }

            }
            R.id.btnCancel ->{
                Utility.sendActionEvent("ApplyOvertimeFragmentCancel_Button_Android",mActivity!!)
                activity?.onBackPressed()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, p3: Long) {

        when (parent.id) {
            R.id.spinnerTransactionType ->{
                val typeModel = leaveList.get(position)
                typeValue = typeModel.text
                if (typeValue.equals("Select Transaction Type")){
                    binding.txtFromSession.isEnabled = false
                    binding.txtToSession.isEnabled = false
                    binding.txtFromSession.text = "Session 1"
                    binding.txtToSession.text = "Session 2"
                    binding.txtFromdate.text = Utility.getCurrentDate()
                    binding.txtTodate.text = Utility.getCurrentDate()
                    startDate = Utility.getCurrentDateSend()
                    endDate = Utility.getCurrentDateSend()
                   // binding.txtApplyingFor.text = "Applying For : 0
                    val text = "Applying For : 0"
                    val spannableText = SpannableString(text)

// Apply bold style and black color to "0"
                    val start = text.indexOf("0")
                    val end = start + 1 // Assuming "0" is always a single digit
                    spannableText.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableText.setSpan(ForegroundColorSpan(Color.BLACK), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // Set the SpannableString to the TextView
                    binding.txtApplyingFor.text = spannableText

                }
                else{
                    binding.txtFromSession.isEnabled = true
                    binding.txtToSession.isEnabled = true
                    getLeaveDays()
                }
            }
            R.id.spinnerManager ->{
                val statusModel = managerList.get(position)
                if(!statusModel.Text.equals("Select Manager"))
                    managerValue = statusModel.Text
                else
                    managerValue = ""
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun getOverTimeFilter() {

        Utility.setWindowNotClickable(mActivity!!.window)
        val loginModel = Login_Helper.getLogin(mActivity)

        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.shimmerFrameLayout.startShimmer()

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<FilterResponseModel> = retrofitApiInterface.getOverTimeFilter(loginModel.access_Token)

        mService.enqueue(object : Callback<FilterResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<FilterResponseModel>, response: Response<FilterResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            binding.shimmerFrameLayout.stopShimmer()
                            val data = filterResponseModel!!.data
                            if(!data.applyingTo.isNullOrEmpty()) {
                                binding.txtApplyingTo.text = data.applyingTo
                                MyApplication.instance.setApplyingTo(data.applyingTo)
                            }
                            leaveList = data.leaveList
                            sessionFromList = data.sessionList
                            sessionToList = data.sessionList
                            managerList = data.managerList

                            MyApplication.instance.setOverTimeLeaveList(leaveList)
                            MyApplication.instance.setSessionFromList(sessionFromList)
                            MyApplication.instance.setSessionToList(sessionToList)
                            MyApplication.instance.setMannagerList(managerList)
                          //  MyApplication.instance.setApplyingTo(data.applyingTo)

                            if(!data.managerEmail.isNullOrEmpty()) {
                                managerEmail = data.managerEmail
                                MyApplication.instance.setManagerEmail(data.managerEmail)
                            }

                            if(leaveList.size > 0)
                                setTypeList(leaveList)

                             if(managerList.size > 0)
                                setManagerList(managerList)

                            binding.spinnerTransactionType.onItemSelectedListener = this@ApplyOvertimeFragment
                            binding.spinnerManager.onItemSelectedListener = this@ApplyOvertimeFragment

                            binding.loutRoot.visibility = View.VISIBLE

                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getOverTimeFilter()
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
                                    mActivity!!,
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
                            mActivity!!,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<FilterResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
                binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    private fun getApplyOverTime() {
        Utility.setWindowNotClickable(mActivity!!.window)
        binding.btnApply.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.leaveType = typeValue
        leaveRequestModel.startDate = startDate+Utility.getcurrentTime()
        leaveRequestModel.endDate = endDate+Utility.getcurrentTime()
        leaveRequestModel.sessionStart = binding.txtFromSession.text.toString()
        leaveRequestModel.sessionEnd = binding.txtToSession.text.toString()
        leaveRequestModel.ccManagerEmail = managerValue
        leaveRequestModel.managerEmail = managerEmail
        leaveRequestModel.mobileNumber = ""
        leaveRequestModel.applyTo = binding.txtApplyingTo.text.toString()
        leaveRequestModel.remarks = binding.edtReason.text.toString()
        leaveRequestModel.workLocation = ""

        val loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getApplyOverTime(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.btnApply.isEnabled = true
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            Utility.playBeep(mActivity!!)
                            Toast.makeText(mActivity!!,baseResponseModel.message, Toast.LENGTH_LONG).show()
                            val intent = Intent(mActivity!!, HomeActivity::class.java)
                            startActivity(intent)
                            mActivity!!.finish()
                        }
                        else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getApplyOverTime()
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            mActivity!!,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveApplyResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.btnApply.isEnabled = true
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun setTypeList(leaveList: ArrayList<LeaveList>) {

        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            leaveList
        )
        binding.spinnerTransactionType.adapter = dropDownAdapter
    }

    private fun setManagerList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            statusList
        )
        binding.spinnerManager.adapter = dropDownAdapter
    }

    private fun getLeaveDays() {
        Utility.setWindowNotClickable(mActivity!!.window)
        binding.mprogress.visibility = View.VISIBLE
        var leaveDaysRequestModel = LeaveDaysRequestModel()

        leaveDaysRequestModel.leaveType = typeValue
        leaveDaysRequestModel.startDate = startDate
        leaveDaysRequestModel.endDate = endDate
        leaveDaysRequestModel.sessionStart = binding.txtFromSession.text.toString()
        leaveDaysRequestModel.sessionEnd = binding.txtToSession.text.toString()
        leaveDaysRequestModel.isOverTime = true
        leaveDaysRequestModel.timeZone = userCountry

        val loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveDaysResponseModel> = retrofitApiInterface.getLeaveDays(loginModel.access_Token,leaveDaysRequestModel)

        mService.enqueue(object : Callback<LeaveDaysResponseModel> {
            override fun onResponse(call: Call<LeaveDaysResponseModel>, response: Response<LeaveDaysResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                try {
                    if (response.isSuccessful) {
                        val leaveDaysResponseModel = response.body()
                        if (leaveDaysResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            val leaveDaysResponseDataModel = leaveDaysResponseModel.data
                          //  binding.txtApplyingFor.text = "Applying For : "+leaveDaysResponseDataModel.Days.toString()

                            val spannable =
                                SpannableStringBuilder(leaveDaysResponseDataModel.Days.toString())
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                spannable.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.BLACK),
                                0,
                                spannable.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            binding.txtApplyingFor.text = "Applying For: "
                            binding.txtApplyingFor.append(spannable)
                        }
                        else {
                            if (leaveDaysResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                           getLeaveDays()
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    leaveDaysResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            mActivity!!,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveDaysResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if(!MyApplication.instance.getUserLocation().isNullOrEmpty()) {
            userCountry = MyApplication.instance.getUserLocation()
        }
        else {
            when {
                PermissionUtils.isAccessFineLocationGranted(mActivity!!) -> {
                    when {
                        PermissionUtils.isLocationEnabled(mActivity!!) -> {
                            snackbar = Utility.showLocationSnackbar(
                                mActivity!!,
                                layoutInflater,
                                mActivity!!.findViewById<View>(android.R.id.content)
                            )
                            setUpLocationListener()
                        }

                        else -> {
//                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            PermissionUtils.showGPSNotEnabledDialog(
                                mActivity!!,
                                "Location service is disabled in your device. Do you want to enable it?",
                                true
                            )
                        }
                    }
                }

                else -> {
                    if (Utility.shouldShowRequestPermissionRationale(
                            mActivity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                        PermissionUtils.showLocationEnabledFromSettingDialog(mActivity!!, true)
                    else {
                        PermissionUtils.requestAccessFineLocationPermission(
                            mActivity as AppCompatActivity,
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
                        PermissionUtils.isLocationEnabled(mActivity!!) -> {
                            setUpLocationListener()
                        }
                        else -> {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            //   PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        mActivity!!,
                        getString(R.string.location_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                    activity?.onBackPressed()
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
        fusedLocationProviderClient = FusedLocationProviderClient(mActivity!!)
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

    private fun showFromSessionPopup(anchorView: View) {
        val popup = ListPopupWindow(mActivity!!)
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            sessionFromList
        )
        popup.setAdapter(dropDownAdapter)
//        popup.setAdapter(
//            ArrayAdapter<String>(
//                mActivity!!,
//                android.R.layout.simple_list_item_1, arrSessionFromList
//            )
//        )
        popup.anchorView = anchorView
        popup.verticalOffset = -120
        popup.setOnItemClickListener { parent, view, position, id ->
            binding.txtFromSession.text = sessionFromList.get(position).text
            getLeaveDays()
            popup.dismiss()
        }
        popup.show()
    }

    private fun showToSessionPopup(anchorView: View) {
        val popup = ListPopupWindow(mActivity!!)
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            sessionToList
        )
        popup.setAdapter(dropDownAdapter)
        popup.anchorView = anchorView
        popup.verticalOffset = -120
        popup.setOnItemClickListener { parent, view, position, id ->
            binding.txtToSession.text = sessionToList.get(position).text
            getLeaveDays()
            popup.dismiss()
        }
        popup.show()
    }

}