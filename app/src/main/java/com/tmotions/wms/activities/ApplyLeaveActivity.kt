package com.tmotions.wms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.*
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.tmotions.wms.R
import com.tmotions.wms.adapters.CalendarAdapter
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.RestrictedDropDownAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.PermissionUtils
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityApplyLeaveBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.HolidayModel
import com.tmotions.wms.models.LeaveApplyResponseModel
import com.tmotions.wms.models.LeaveRequestModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leavedays.LeaveDaysRequestModel
import com.tmotions.wms.models.leavedays.LeaveDaysResponseModel
import com.tmotions.wms.models.restrictedholidaymodels.RestrictedDataListModel
import com.tmotions.wms.models.restrictedholidaymodels.RestrictedHolidayModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.ParseException
import java.util.*


class ApplyLeaveActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener{

    lateinit var binding: ActivityApplyLeaveBinding
    private var leaveList = ArrayList<LeaveList>()
    private var sessionFromList = ArrayList<LeaveList>()
    private var sessionToList = ArrayList<LeaveList>()
    private var managerList = ArrayList<LeaveList>()
    private var restrictedLeaveList = ArrayList<RestrictedDataListModel>()
    var startDate = ""
    var endDate = ""
    var typeValue = ""
    var managerValue = ""
    var leaveType = ""
    var displaydate = ""
    var applyingTo = ""
    var rhManagerValue = ""
    var rhLeaveType = ""
    var rhDate = ""
    var rhReason = ""
    var managerEmail = ""
    var holidayName = ""
    var balanceAvailable = 0.0
    var month: GregorianCalendar? = null
    var itemmonth: GregorianCalendar? = null
    val arrlist_calendar = ArrayList<HolidayModel>()
    var snackbar: Snackbar? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }
    var userCountry = ""
    private var isHalfday = false
    private var isSwitchEnable = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_apply_leave)

        binding.headerLayout.txtTitle.text = "Apply Request"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }

        MyApplication.instance.activity = this
        Utility.sendViewScreenEvent("ApplyLeave_Activity_Android",this)
        month = GregorianCalendar.getInstance() as GregorianCalendar
        itemmonth = month!!.clone() as GregorianCalendar
        var adapter = CalendarAdapter(this, month, arrlist_calendar)

        binding.spinTransaction.onItemSelectedListener = this
        binding.spinnerManager.onItemSelectedListener = this

        Utility.handleMultilineEditText(binding.edtReason)
        binding.edtReason.filters = arrayOf(Utility.ignoreFirstWhiteSpace())
        binding.edtWorkLocation.filters = arrayOf(Utility.ignoreFirstWhiteSpace())

        leaveType = intent.getStringExtra("leaveType").toString()
        displaydate = intent.getStringExtra("displayDate").toString()
        holidayName = intent.getStringExtra("holidayName").toString()

        if(!displaydate.isNullOrEmpty()){
            binding.txtFromDate.text = displaydate
            binding.txtToDate.text = displaydate
            startDate = intent.getStringExtra("sendDate").toString()
            endDate = intent.getStringExtra("sendDate").toString()
        }else{
            binding.txtFromDate.text = Utility.getCurrentDate()
            binding.txtToDate.text = Utility.getCurrentDate()
            startDate = Utility.getCurrentDateSend()
            endDate = Utility.getCurrentDateSend()
        }

        binding.imgFromdate.setOnClickListener(this)
        binding.imgTodate.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnApply.setOnClickListener(this)
        binding.txtFromSession.setOnClickListener(this)
        binding.txtToSession.setOnClickListener(this)
        binding.headerLayout.imgBack.setOnClickListener(this)

        if(MyApplication.instance.getLeaveList().size > 0){
            leaveList = MyApplication.instance.getLeaveList()
            sessionFromList = MyApplication.instance.getSessionFromList()
            sessionToList = MyApplication.instance.getSessionToList()
            managerList = MyApplication.instance.getManagerList()
            managerEmail =  MyApplication.instance.getManagerEmail()
            setTypeList(leaveList)
            setManagerList(managerList)
            binding.shimmerFrameLayout.visibility = View.GONE
            binding.shimmerFrameLayout.stopShimmer()
            binding.loutRoot.visibility = View.VISIBLE
            applyingTo = MyApplication.instance.getApplyingTo()
            binding.txtApplyingTo.text = applyingTo
            if(leaveType == "Restricted Holiday")
                displayRHDialog()

            if(leaveType.equals("Earned Leave"))
                binding.switchHalfDay.isEnabled = false
        }else {
            getLeaveFilter()
        }

        binding.switchHalfDay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            isSwitchEnable = isChecked
            if (isChecked) {
                isHalfday = true
                binding.loutToSession.visibility = View.GONE
                binding.lblFromSession.text = "Session"
                binding.txtToSession.setTextColor(ContextCompat.getColor(this@ApplyLeaveActivity, R.color.gray))
                binding.lblToSession.setTextColor(ContextCompat.getColor(this@ApplyLeaveActivity, R.color.gray))

                if(typeValue == "Restricted Holiday" || typeValue == "Earned Leave") {
                }
                else
                    getLeaveDays()
            }
            else {
                isHalfday = false
                binding.lblFromSession.text = "From Session"
                binding.txtToSession.setTextColor(ContextCompat.getColor(this@ApplyLeaveActivity, R.color.black))
                binding.lblToSession.setTextColor(ContextCompat.getColor(this@ApplyLeaveActivity, R.color.black))
                //  binding.txtToSession.isEnabled = true
                binding.loutToSession.visibility = View.VISIBLE
                if(typeValue == "Restricted Holiday" || typeValue == "Earned Leave") {
                }
                else
                    getLeaveDays()
            }
        })

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
               // Utility.displayAlertDialog(this,"User location is->"+userCountry,layoutInflater)
                Utility.sendActionEvent("ApplyLeave_Button_Android",this)
                if (typeValue.equals("Select Transaction Type")) {
                    Utility.displayAlertDialog(this,resources.getString(R.string.please_choose_a_transaction_type),layoutInflater)
                }
                else if (typeValue.equals("WFH") && binding.edtWorkLocation.text.isNullOrEmpty()) {
                    Utility.displayAlertDialog(this,resources.getString(R.string.kindly_input_the_work_location),layoutInflater)
                }else if (userCountry.isNullOrEmpty()) {
                    Utility.displayAlertDialog(this,"User location not found ",layoutInflater)
                }
                else {
                    if (Utility.isOnline(this)) {
                        getApplyLeave()
                    } else {
                        Utility.displayAlertDialog(this,resources.getString(R.string.nointernet),layoutInflater)
                    }
                }
            }
            R.id.btnCancel ->{
                Utility.sendActionEvent("CancelLeave_Button_Android",this)
                finish()
            }
            R.id.imgBack -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getLeaveFilter() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@ApplyLeaveActivity)
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
                            if(!data.applyingTo.isNullOrEmpty()) {
                                applyingTo = data.applyingTo
                                binding.txtApplyingTo.text = applyingTo
                                MyApplication.instance.setApplyingTo(applyingTo)
                            }

                            leaveList = data.leaveList
                            sessionFromList = data.sessionList
                            sessionToList = data.sessionList
                            managerList = data.managerList

                            MyApplication.instance.setLeaveList(leaveList)
                            MyApplication.instance.setSessionFromList(sessionFromList)
                            MyApplication.instance.setSessionToList(sessionToList)
                            MyApplication.instance.setMannagerList(managerList)
                         //   MyApplication.instance.setApplyingTo(data.applyingTo)

                            if (!data.managerEmail.isNullOrEmpty()) {
                                managerEmail = data.managerEmail
                                MyApplication.instance.setManagerEmail(data.managerEmail)
                            }

                            if(leaveList.size > 0){
                                setTypeList(leaveList)
                            }

                            if(managerList.size > 0)
                                setManagerList(managerList)

                            binding.loutRoot.visibility = View.VISIBLE
                            if(leaveType.equals("Restricted Holiday"))
                                displayRHDialog()

                            if(leaveType.equals("Earned Leave"))
                               binding.switchHalfDay.isEnabled = false

                        }
                        else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ApplyLeaveActivity,
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
                                    this@ApplyLeaveActivity,
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
                            this@ApplyLeaveActivity,
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
                Utility.displayAlertDialog(this@ApplyLeaveActivity, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
                binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    private fun setTypeList(leaveList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            this@ApplyLeaveActivity,
            leaveList
        )
        binding.spinTransaction.adapter = dropDownAdapter
//        if(!leaveType.isNullOrEmpty() && !leaveType.equals("Restricted Holiday")) {
//            val endTimeIndex = selectSpinnerValueTime(leaveList, leaveType)
//            binding.spinTransaction.setSelection(endTimeIndex, true)
//            val typeModel = leaveList.get(endTimeIndex)
//            typeValue = typeModel.Text
//            balanceAvailable = typeModel.Value.toDouble()
//            binding.txtTotalLeave.text = "Balance : " + typeModel.Value
//        }
        if(!leaveType.isNullOrEmpty()) {
            val endTimeIndex = selectSpinnerValueTime(leaveList, leaveType)
            binding.spinTransaction.setSelection(endTimeIndex, true)
            val typeModel = leaveList.get(endTimeIndex)
            typeValue = typeModel.Text
            balanceAvailable = typeModel.Value.toDouble()

            val balanceText = typeModel.Value.toString()
            val spannable = SpannableString(balanceText)
            spannable.setSpan(ForegroundColorSpan(Color.BLACK), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.txtTotalLeave.text = SpannableStringBuilder().append("Balance : ").append(spannable)
        }
    }

    private fun setManagerList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            this@ApplyLeaveActivity,
            statusList
        )
        binding.spinnerManager.adapter = dropDownAdapter
    }

    private fun getApplyLeave() {
        Utility.setWindowNotClickable(window)
        binding.btnApply.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.leaveType = typeValue
        leaveRequestModel.startDate = startDate+Utility.getcurrentTime()
        leaveRequestModel.endDate = endDate+Utility.getcurrentTime()
        if(isHalfday) {
            leaveRequestModel.sessionStart = binding.txtFromSession.text.toString()
            leaveRequestModel.sessionEnd = binding.txtFromSession.text.toString()
        }
        else{
            leaveRequestModel.sessionStart = binding.txtFromSession.text.toString()
            leaveRequestModel.sessionEnd = binding.txtToSession.text.toString()
        }
        leaveRequestModel.ccManagerEmail = managerValue
        leaveRequestModel.managerEmail = managerEmail
        leaveRequestModel.mobileNumber = binding.edtMobileNumber.text.toString()
        leaveRequestModel.applyTo = binding.txtApplyingTo.text.toString()
        leaveRequestModel.remarks = binding.edtReason.text.toString()
        leaveRequestModel.workLocation = binding.edtWorkLocation.text.toString()
        leaveRequestModel.timeZone = userCountry
        leaveRequestModel.isHalfday = isHalfday

        val loginModel = Login_Helper.getLogin(this@ApplyLeaveActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getApplyLeave(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(window)
                binding.btnApply.isEnabled = true
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            leaveList.clear()
                            binding.mprogress.visibility = View.GONE
                            Utility.playBeep(this@ApplyLeaveActivity)
                            Toast.makeText(this@ApplyLeaveActivity,baseResponseModel.message,Toast.LENGTH_LONG).show()
                            val intent = Intent(this@ApplyLeaveActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ApplyLeaveActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getApplyLeave()
                                        }
                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ApplyLeaveActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ApplyLeaveActivity,
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
                Utility.displayAlertDialog(this@ApplyLeaveActivity, "$t", layoutInflater)
                binding.btnApply.isEnabled = true
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, p3: Long) {

        when (parent.id) {
            R.id.spinTransaction ->{
                val typeModel = leaveList.get(position)
                typeValue = typeModel.Text
                val balanceText = typeModel.Value.toString()
                val spannable = SpannableString(balanceText)
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(Color.BLACK), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.txtTotalLeave.text = SpannableStringBuilder().append("Balance : ").append(spannable)

                balanceAvailable = typeModel.Value.toDouble()
                //binding.txtTotalLeave.text = "Balance : "+typeModel.Value

                if (typeValue.equals("Select Transaction Type")){
                    binding.txtFromSession.isEnabled = false
                    binding.txtToSession.isEnabled = false
                    binding.switchHalfDay.isEnabled = false
                    binding.loutWorkLocation.visibility = View.GONE
                    binding.txtTotalLeave.visibility = View.GONE
                    binding.loutMobileNumber.visibility = View.VISIBLE
                }
                else{
                    if(typeValue.equals("Restricted Holiday")) {
                        binding.switchHalfDay.isChecked = false
                        isHalfday = false
                        displayRHDialog()
                    }
                    else {
                        binding.txtFromSession.text = "Session 1"
                        binding.txtToSession.text = "Session 2"
                     //   getLeaveDays()
                        binding.txtTotalLeave.visibility = View.VISIBLE
                        if (typeValue.equals("Earned Leave")) {
                            binding.txtFromSession.isEnabled = false
                            binding.txtToSession.isEnabled = false
                            binding.switchHalfDay.isEnabled = false
                            binding.loutWorkLocation.visibility = View.GONE
                            binding.loutMobileNumber.visibility = View.VISIBLE
                            binding.switchHalfDay.isChecked = false
                            isHalfday = false
                            if(!isSwitchEnable)
                                getLeaveDays()
                        }
                        else if(typeValue.equals("WFH")){
                            binding.loutWorkLocation.visibility = View.VISIBLE
                            binding.loutMobileNumber.visibility = View.GONE
                            binding.txtFromSession.isEnabled = true
                            binding.txtToSession.isEnabled = true
                            binding.switchHalfDay.isEnabled = true
                            binding.txtTotalLeave.visibility = View.GONE
                            getLeaveDays()
                        }
                        else{
                            binding.txtFromSession.isEnabled = true
                            binding.txtToSession.isEnabled = true
                            binding.switchHalfDay.isEnabled = true
                            binding.loutWorkLocation.visibility = View.GONE
                            binding.loutMobileNumber.visibility = View.VISIBLE
                            getLeaveDays()
                        }
                     }
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

    private fun selectSpinnerValueTime(ListSpinner: ArrayList<LeaveList>, myString: String): Int {
        var index = 0
        for (i in ListSpinner.indices) {
            if (ListSpinner[i].Text.equals(myString)) {
                index = i
                break
            }
        }
        return index
    }

    private fun getLeaveDays() {
        Utility.setWindowNotClickable(window)
        binding.mprogress.visibility = View.VISIBLE
        var leaveDaysRequestModel = LeaveDaysRequestModel()

        leaveDaysRequestModel.leaveType = typeValue
        leaveDaysRequestModel.startDate = startDate+Utility.getcurrentTime()
        leaveDaysRequestModel.endDate = endDate+Utility.getcurrentTime()
        if(isHalfday) {
            leaveDaysRequestModel.sessionStart = binding.txtFromSession.text.toString()
            leaveDaysRequestModel.sessionEnd = binding.txtFromSession.text.toString()
        }else{
            leaveDaysRequestModel.sessionStart = binding.txtFromSession.text.toString()
            leaveDaysRequestModel.sessionEnd = binding.txtToSession.text.toString()
        }
        leaveDaysRequestModel.isOverTime = false
        leaveDaysRequestModel.timeZone = userCountry
        leaveDaysRequestModel.isHalfday = isHalfday

        val loginModel = Login_Helper.getLogin(this@ApplyLeaveActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveDaysResponseModel> = retrofitApiInterface.getLeaveDays(loginModel.access_Token,leaveDaysRequestModel)

        mService.enqueue(object : Callback<LeaveDaysResponseModel> {
            override fun onResponse(call: Call<LeaveDaysResponseModel>, response: Response<LeaveDaysResponseModel>) {
                /* binding.mprogress.visibility = View.GONE*/
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val leaveDaysResponseModel = response.body()
                        if (leaveDaysResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            val leaveDaysResponseDataModel = leaveDaysResponseModel.data
                         //   binding.txtApplyingFor.text = "Applying For : "+leaveDaysResponseDataModel.Days.toString()
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

                            if (leaveDaysResponseDataModel.Days != 0.0) {
                                if (typeValue == "Earned Leave" || typeValue == "Casual Leave" || typeValue == "Sick Leave" || typeValue == "Comp Off") { // Check if it's one of the specified leave types
                                    if (balanceAvailable == 0.0) {
                                        Utility.displayAlertDialog(
                                            this@ApplyLeaveActivity!!,
                                            resources.getString(R.string.enough_balance),
                                            layoutInflater
                                        )
                                    }
                                }
                            }
                        }
                        else {
                            if (leaveDaysResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ApplyLeaveActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveDays()
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                val leaveDaysResponseDataModel = leaveDaysResponseModel.data
                               // binding.txtApplyingFor.text = "Applying For : "+leaveDaysResponseDataModel.Days.toString()
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
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ApplyLeaveActivity,
                                    leaveDaysResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ApplyLeaveActivity,
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
                Utility.displayAlertDialog(this@ApplyLeaveActivity, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun displayRHDialog() {
        val alertDialog = AlertDialog.Builder(this)
        val alert: AlertDialog = alertDialog.create()
        val convertView = layoutInflater.inflate(R.layout.restricted_leave_popup, null) as View
        alert.setCancelable(false)
        alert.setView(convertView)
        val imgClose = convertView.findViewById<ImageView>(R.id.imgClose)
        val shimmerFrameLayout = convertView.findViewById<ShimmerFrameLayout>(R.id.shimmerFrameLayout)
        val loutRoot = convertView.findViewById<LinearLayout>(R.id.loutRoot)
        val spinRestricted = convertView.findViewById<Spinner>(R.id.spinRestricted)
        val txtApplyingTo = convertView.findViewById<TextView>(R.id.txtApplyingTo)
        val spinnerManager = convertView.findViewById<Spinner>(R.id.spinnerManager)
        val txtApplyingDate = convertView.findViewById<TextView>(R.id.txtApplyingDate)
        val txtBalance = convertView.findViewById<TextView>(R.id.txtBalance)
        val txtRHDate = convertView.findViewById<TextView>(R.id.txtRHDate)
        val edtRhReason = convertView.findViewById<EditText>(R.id.edtRhReason)
        val btnCancel = convertView.findViewById<Button>(R.id.btnCancel)
        val btnApply = convertView.findViewById<Button>(R.id.btnApply)
        val rhProgress = convertView.findViewById<LinearProgressIndicator>(R.id.rhProgress)

        Utility.handleMultilineEditText(edtRhReason)
        edtRhReason.setFilters(arrayOf(Utility.ignoreFirstWhiteSpace()))
        loutRoot.setOnClickListener(View.OnClickListener {
            Utility.hideKeyboard(this,loutRoot)
        })

        txtApplyingDate.text = Html.fromHtml(Utility.getColoredSpanned("Applying Date:", "#104578") +" "+  Utility.getColoredSpanned(Utility.getCurrentDate().toString(), "#000000"))
        txtApplyingTo.text = applyingTo

        if(managerList.size > 0)
            setManagerRestrictedList(managerList,spinnerManager)

        if(Utility.isOnline(this)) {
            getRestrictedHolidayList(shimmerFrameLayout,loutRoot,spinRestricted,alert)
        }
        else
            Utility.displayAlertDialog(
                this,
                resources.getString(R.string.nointernet),
                layoutInflater
            )

        imgClose.setOnClickListener {
            alert.cancel()
            finish()
        }

        btnCancel.setOnClickListener {
            alert.cancel()
            finish()
        }

        spinRestricted.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var restrictedHolidayModel = restrictedLeaveList.get(position)
                rhLeaveType = restrictedHolidayModel.holiday
                rhDate = Utility.sendRHDate(restrictedHolidayModel.date)
                txtBalance.text = Html.fromHtml(Utility.getColoredSpanned("Balance:", "#104578") +" "+  Utility.getColoredSpanned(restrictedHolidayModel.balance.toString(), "#000000"))
                txtRHDate.text = Html.fromHtml(Utility.getColoredSpanned("RH Date:", "#104578") +" "+  Utility.getColoredSpanned(restrictedHolidayModel.date, "#000000"))
            }
        }

        btnApply.setOnClickListener(View.OnClickListener {
            rhReason = edtRhReason.text.toString()

            if (Utility.isOnline(this)) {
                getApplyRHLeave(rhProgress)
            } else {
                Utility.displayAlertDialog(this,resources.getString(R.string.nointernet),layoutInflater)
            }
        })
        alert.show()
    }

    private fun getApplyRHLeave(rhProgress: LinearProgressIndicator) {
        Utility.setWindowNotClickable(window)
        rhProgress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.leaveType = "Restricted Holiday"
        leaveRequestModel.startDate = rhDate+Utility.getcurrentTime()
        leaveRequestModel.endDate = rhDate+Utility.getcurrentTime()
        leaveRequestModel.sessionStart = "Session 1"
        leaveRequestModel.sessionEnd = "Session 2"
        leaveRequestModel.ccManagerEmail = rhManagerValue
        leaveRequestModel.managerEmail = managerEmail
        leaveRequestModel.mobileNumber = ""
        leaveRequestModel.applyTo = applyingTo
        leaveRequestModel.remarks = rhReason
        leaveRequestModel.workLocation = ""
        leaveRequestModel.timeZone = userCountry
        leaveRequestModel.isHalfday = false

        val loginModel = Login_Helper.getLogin(this@ApplyLeaveActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getApplyLeave(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            rhProgress.visibility = View.GONE
                            Toast.makeText(this@ApplyLeaveActivity,baseResponseModel.message,Toast.LENGTH_LONG).show()
                            val intent = Intent(this@ApplyLeaveActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ApplyLeaveActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getApplyLeave()
                                        }
                                        override fun onFailure() {
                                            rhProgress.visibility = View.GONE
                                        }
                                    })
                            else {
                                rhProgress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ApplyLeaveActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        rhProgress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ApplyLeaveActivity,
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
                Utility.displayAlertDialog(this@ApplyLeaveActivity, "$t", layoutInflater)
                rhProgress.visibility = View.GONE
            }
        })
    }

    private fun setManagerRestrictedList(statusList: ArrayList<LeaveList>, spinnerManager: Spinner) {

        val dropDownAdapter = DropDownAdapter(this,statusList)
        spinnerManager.adapter = dropDownAdapter

        spinnerManager.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val statusModel = managerList.get(position)
                if(!statusModel.Text.equals("Select Manager"))
                    rhManagerValue = statusModel.Text
                else
                    rhManagerValue = ""
            }
        }
    }

    private fun getRestrictedHolidayList(
        shimmerFrameLayout: ShimmerFrameLayout,
        loutRoot: LinearLayout,
        spinRestricted: Spinner,
        alert: AlertDialog
    ) {
        Utility.setWindowNotClickable(window)
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()

        val loginModel = Login_Helper.getLogin(this@ApplyLeaveActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<RestrictedHolidayModel> = retrofitApiInterface.getRestrictedHolidayList(loginModel.access_Token)

        mService.enqueue(object : Callback<RestrictedHolidayModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<RestrictedHolidayModel>, response: Response<RestrictedHolidayModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            shimmerFrameLayout.visibility = View.GONE
                            shimmerFrameLayout.stopShimmer()
                            restrictedLeaveList = filterResponseModel!!.data

                            if(restrictedLeaveList.size > 0){
                                setRestrictedTypeList(restrictedLeaveList,spinRestricted)
                            }

                            loutRoot.visibility = View.VISIBLE

                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ApplyLeaveActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveFilter()
                                        }

                                        override fun onFailure() {
                                            shimmerFrameLayout.visibility = View.GONE
                                            shimmerFrameLayout.stopShimmer()
                                        }
                                    })
                            else {
                                alert.cancel()
                                shimmerFrameLayout.visibility = View.GONE
                                shimmerFrameLayout.stopShimmer()
                                Utility.displayAlertDialog(
                                    this@ApplyLeaveActivity,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        shimmerFrameLayout.visibility = View.GONE
                        shimmerFrameLayout.stopShimmer()
                        Utility.displayAlertDialog(
                            this@ApplyLeaveActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RestrictedHolidayModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ApplyLeaveActivity, "$t", layoutInflater)
                shimmerFrameLayout.visibility = View.GONE
                shimmerFrameLayout.stopShimmer()
            }
        })
    }
    private fun setRestrictedTypeList(
        leaveList: ArrayList<RestrictedDataListModel>,
        spinRestricted: Spinner
    ) {

        val dropDownAdapter = RestrictedDropDownAdapter(
            this,
            leaveList
        )
        spinRestricted.adapter = dropDownAdapter

        if(holidayName.isNullOrEmpty()) {
            val endTimeIndex = selectRestrictedLeaveTypeSpinner(leaveList, true)
            spinRestricted.setSelection(endTimeIndex, true)
        }else{
            val endTimeIndex = selectRestrictedLeaveTypeSpinner(leaveList, holidayName)
            spinRestricted.setSelection(endTimeIndex, true)
        }

    }

    private fun selectRestrictedLeaveTypeSpinner(ListSpinner: ArrayList<RestrictedDataListModel>, isSelected: Boolean): Int {
        var index = 0
        for (i in ListSpinner.indices) {
            if (ListSpinner[i].Selected) {
                index = i
                break
            }
        }
        return index
    }

    private fun selectRestrictedLeaveTypeSpinner(ListSpinner: ArrayList<RestrictedDataListModel>, holidayName: String): Int {
        var index = 0
        for (i in ListSpinner.indices) {
            if (ListSpinner[i].Holiday.equals(holidayName)) {
                index = i
                break
            }
        }
        return index
    }

   /*    private fun setUpLocationListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(1000).setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                      //  userCountry = Utility.getAddressFromCoordinates(location.latitude, location.longitude)
                        getAddressFromCoordinates(location.latitude, location.longitude)
//                        val geocoder = Geocoder(MyApplication.instance, Locale.getDefault())
//                        val list: List<Address> = geocoder.getFromLocation(
//                            location.latitude,
//                            location.longitude!!,1) as List<Address>
//                            if(list.isNotEmpty())
//                            userCountry = list[0].countryName
                           // Log.d("loccccc....", userCountry)
                    }
                }
            },
            Looper.myLooper()
        )
    }

    override fun onStart() {
        super.onStart()
           when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        setUpLocationListener()
                    }
                    else -> {
//                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        PermissionUtils.showGPSNotEnabledDialog(this@ApplyLeaveActivity,"Location service is disabled in your device. Do you want to enable it?",false)
                    }
                }
            }
            else -> {
                if (Utility.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                    PermissionUtils.showLocationEnabledFromSettingDialog(this,false)
                else {
                    PermissionUtils.requestAccessFineLocationPermission(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
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
//                    Toast.makeText(
//                        this,
//                        getString(R.string.location_permission_not_granted),
//                        Toast.LENGTH_LONG
//                    ).show()
                 //   PermissionUtils.showLocationEnabledFromSettingDialog(this,layoutInflater)
                }
            }
        }
    }*/

    private fun showFromSessionPopup(anchorView: View) {
        val popup = ListPopupWindow(this)
        val dropDownAdapter = DropDownAdapter(
            this,
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
        val popup = ListPopupWindow(this)
        val dropDownAdapter = DropDownAdapter(
            this,
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
                                this@ApplyLeaveActivity,
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
         /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

            if(snackbar!=null){
               snackbar!!.dismiss()
            }

        } catch (e: IOException) {
             Log.e("sssssssssssss","Check internet: ${e.message}")
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}