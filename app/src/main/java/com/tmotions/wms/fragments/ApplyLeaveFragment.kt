package com.tmotions.wms.fragments

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
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.*
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.tmotions.wms.R
import com.tmotions.wms.activities.HomeActivity
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.RestrictedDropDownAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.PermissionUtils
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.FragmentApplyLeaveBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
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


class ApplyLeaveFragment : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentApplyLeaveBinding
    var mActivity: FragmentActivity? = null
    private var leaveList = ArrayList<LeaveList>()
    private var restrictedLeaveList = ArrayList<RestrictedDataListModel>()
    private var sessionFromList = ArrayList<LeaveList>()
    private var sessionToList = ArrayList<LeaveList>()
    private var managerList = ArrayList<LeaveList>()
    var startDate = ""
    var endDate = ""
    var typeValue = ""
    var managerValue = ""
    var leaveType = ""
    var managerEmail = ""
    var applyingTo = ""
    var rhLeaveType = ""
    var rhDate = ""
    var rhManagerValue = ""
    var rhReason = ""
    var balanceAvailable = 0.0
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


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apply_leave, container, false)

        MyApplication.instance.activity = mActivity!! as AppCompatActivity
        Utility.sendViewScreenEvent("ApplyLeave_Fragment_Android",mActivity!!)
        Utility.handleMultilineEditText(binding.edtReason)
        binding.edtReason.setFilters(arrayOf(Utility.ignoreFirstWhiteSpace()))
        binding.edtWorkLocation.setFilters(arrayOf(Utility.ignoreFirstWhiteSpace()))

        binding.txtFromDate.text = Utility.getCurrentDate()
        binding.txtToDate.text = Utility.getCurrentDate()
        startDate = Utility.getCurrentDateSend()
        endDate = Utility.getCurrentDateSend()

        binding.spinTransaction.onItemSelectedListener = this@ApplyLeaveFragment
        binding.spinnerManager.onItemSelectedListener = this@ApplyLeaveFragment

        binding.imgFromdate.setOnClickListener(this)
        binding.imgTodate.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnApply.setOnClickListener(this)
        binding.txtFromSession.setOnClickListener(this)
        binding.txtToSession.setOnClickListener(this)

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
        }
        else {
            getLeaveFilter()
        }

        binding.switchHalfDay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            isSwitchEnable = isChecked
            if (isChecked) {
                isHalfday = true
                binding.loutToSession.visibility = View.GONE
                binding.lblFromSession.text = "Session"
                binding.txtToSession.setTextColor(ContextCompat.getColor(mActivity!!, R.color.gray))
                binding.lblToSession.setTextColor(ContextCompat.getColor(mActivity!!, R.color.gray))
                if(typeValue == "Restricted Holiday" || typeValue == "Earned Leave") {
                }
                else
                    getLeaveDays()
            }
            else {
                isHalfday = false
                binding.lblFromSession.text = "From Session"
                binding.txtToSession.setTextColor(ContextCompat.getColor(mActivity!!, R.color.black))
                binding.lblToSession.setTextColor(ContextCompat.getColor(mActivity!!, R.color.black))
              //  binding.txtToSession.isEnabled = true
                binding.loutToSession.visibility = View.VISIBLE
                Toast.makeText(mActivity!!,typeValue,Toast.LENGTH_LONG).show()
                if(typeValue == "Restricted Holiday" || typeValue == "Earned Leave") {
                }
                else
                    getLeaveDays()

            }
        })

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgFromdate -> {
                Utility.sendActionEvent("ApplyLeaveFragmentFromDate_Button_Android",mActivity!!)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    mActivity!!,
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
                Utility.sendActionEvent("ApplyLeaveFragmentToDate_Button_Android",mActivity!!)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    mActivity!!,
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
              //  Utility.displayAlertDialog(mActivity!!,"location = > "+userCountry,layoutInflater)
                if (userCountry.isNullOrEmpty()) {
                    Utility.displayAlertDialog(mActivity!!,"User location not found ",layoutInflater)
                }
                else{
                      if (typeValue.equals("Select Transaction Type")) {
                          Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.please_choose_a_transaction_type),layoutInflater)
                      }
                      else if (typeValue.equals("WFH") && binding.edtWorkLocation.text.isNullOrEmpty()) {
                          Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.kindly_input_the_work_location),layoutInflater)
                      }
                      else {
                          if (Utility.isOnline(mActivity!!)) {
                              Utility.sendActionEvent("ApplyLeaveFragmentApply_Button_Android",mActivity!!)
                              getApplyLeave()
                          } else {
                              Utility.displayAlertDialog(mActivity!!,resources.getString(R.string.nointernet),layoutInflater)
                          }
                      }
                  }
            }
            R.id.btnCancel ->{
                Utility.sendActionEvent("ApplyLeaveFragmentCancel_Button_Android",mActivity!!)
                activity?.onBackPressed()
            }
        }
    }

    private fun getLeaveFilter() {
        Utility.setWindowNotClickable(mActivity!!.window)
        val loginModel = Login_Helper.getLogin(mActivity)
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.shimmerFrameLayout.startShimmer()

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<FilterResponseModel> = retrofitApiInterface.getLeaveFilter(loginModel.access_Token)

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
                        //    MyApplication.instance.setApplyingTo(data.applyingTo)
                            if(!data.managerEmail.isNullOrEmpty()) {
                                managerEmail = data.managerEmail
                                MyApplication.instance.setManagerEmail(data.managerEmail)
                            }

                            if(leaveList.size > 0){
                                setTypeList(leaveList)
                            }

                            if(managerList.size > 0)
                                setManagerList(managerList)

                            binding.loutRoot.visibility = View.VISIBLE

                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
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

    private fun setTypeList(leaveList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            leaveList
        )
        binding.spinTransaction.adapter = dropDownAdapter
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

    private fun setRestrictedTypeList(
        leaveList: ArrayList<RestrictedDataListModel>,
        spinRestricted: Spinner
    ) {
        val dropDownAdapter = RestrictedDropDownAdapter(
            mActivity!!,
            leaveList
        )
        spinRestricted.adapter = dropDownAdapter

        val endTimeIndex = selectRestrictedLeaveTypeSpinner(leaveList, true)
        spinRestricted.setSelection(endTimeIndex, true)

    }

    private fun setManagerList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            statusList
        )
        binding.spinnerManager.adapter = dropDownAdapter
    }

    private fun setManagerRestrictedList(statusList: ArrayList<LeaveList>, spinnerManager: Spinner) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            statusList
        )
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

    private fun getApplyLeave() {
        Utility.setWindowNotClickable(mActivity!!.window)
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

        val loginModel = Login_Helper.getLogin(mActivity)
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getApplyLeave(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.btnApply.isEnabled = true
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            leaveList.clear()
                            binding.mprogress.visibility = View.GONE
                            Toast.makeText(mActivity!!,baseResponseModel.message,Toast.LENGTH_LONG).show()
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
                                            getApplyLeave()
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

    private fun getApplyRHLeave(rhProgress: LinearProgressIndicator) {
        Utility.setWindowNotClickable(mActivity!!.window)
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

        val loginModel = Login_Helper.getLogin(mActivity)
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getApplyLeave(loginModel.access_Token,leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            rhProgress.visibility = View.GONE
                            Toast.makeText(mActivity!!,baseResponseModel.message,Toast.LENGTH_LONG).show()
                            Utility.playBeep(mActivity!!)
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
                                            getApplyLeave()
                                        }

                                        override fun onFailure() {
                                            rhProgress.visibility = View.GONE
                                        }
                                    })
                            else {
                                rhProgress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        rhProgress.visibility = View.GONE
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
                rhProgress.visibility = View.GONE
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.spinTransaction -> handleTransactionItemSelected(position)
            R.id.spinnerManager -> handleManagerItemSelected(position)
        }
    }

    private fun handleTransactionItemSelected(position: Int) {
        val typeModel = leaveList[position]
        typeValue = typeModel.Text
        val balanceText = typeModel.Value.toString()
        val spannable = SpannableString(balanceText)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(Color.BLACK), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.txtTotalLeave.text = SpannableStringBuilder().append("Balance : ").append(spannable)
//binding.txtTotalLeave.text = "Balance: " + typeModel.Value
      //  balanceAvailable = typeModel.Value.toDouble()
      //  binding.txtTotalLeave.text = "Balance: " + typeModel.Value
        balanceAvailable = typeModel.Value.toDouble()
        if (typeValue == "Restricted Holiday") {
            binding.switchHalfDay.isChecked = false
            isHalfday = false
            displayRHDialog()
        } else {
            if (typeValue == "Select Transaction Type") {
                binding.txtFromSession.isEnabled = false
                binding.txtToSession.isEnabled = false
                binding.switchHalfDay.isEnabled = false
                binding.loutWorkLocation.visibility = View.GONE
                binding.txtTotalLeave.visibility = View.GONE
                binding.loutMobileNumber.visibility = View.VISIBLE

            } else {
               // binding.txtTotalLeave.visibility = View.VISIBLE
                binding.txtFromSession.text = "Session 1"
                binding.txtToSession.text = "Session 2"
//                getLeaveDays()
                when (typeValue) {
                    "Earned Leave" -> {
                        binding.txtFromSession.isEnabled = false
                        binding.txtToSession.isEnabled = false
                        binding.switchHalfDay.isEnabled = false
                        binding.loutWorkLocation.visibility = View.GONE
                        binding.loutMobileNumber.visibility = View.VISIBLE
                        isHalfday = false
                        if(!isSwitchEnable)
                        getLeaveDays()
                    }
                    "WFH" -> {
                        binding.loutWorkLocation.visibility = View.VISIBLE
                        binding.loutMobileNumber.visibility = View.GONE
                        binding.txtFromSession.isEnabled = true
                        binding.txtToSession.isEnabled = true
                        binding.switchHalfDay.isEnabled = true
                        binding.txtTotalLeave.visibility = View.GONE
                        getLeaveDays()
                    }
                    else -> {
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
    }

    private fun handleManagerItemSelected(position: Int) {
        val statusModel = managerList[position]
        managerValue = if (statusModel.Text != "Select Manager") statusModel.Text else ""
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
        Utility.setWindowNotClickable(mActivity!!.window)
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

        val loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveDaysResponseModel> = retrofitApiInterface.getLeaveDays(loginModel.access_Token,leaveDaysRequestModel)

        mService.enqueue(object : Callback<LeaveDaysResponseModel> {
            override fun onResponse(call: Call<LeaveDaysResponseModel>, response: Response<LeaveDaysResponseModel>) {
              /*  binding.mprogress.visibility = View.GONE*/
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

                            if (leaveDaysResponseDataModel.Days != 0.0) {
                                if (typeValue == "Earned Leave" || typeValue == "Casual Leave" || typeValue == "Sick Leave" || typeValue == "Comp Off") { // Check if it's one of the specified leave types
                                    if (balanceAvailable == 0.0) {
                                        Utility.displayAlertDialog(
                                            mActivity!!,
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

    private fun displayRHDialog() {
        val alertDialog = AlertDialog.Builder(mActivity!!)
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
            Utility.hideKeyboard(mActivity!!,loutRoot)
        })

        txtApplyingDate.text = Html.fromHtml(Utility.getColoredSpanned("Applying Date:", "#104578") +" "+  Utility.getColoredSpanned(Utility.getCurrentDate().toString(), "#000000"))
        txtApplyingTo.text = applyingTo

        if(managerList.size > 0)
            setManagerRestrictedList(managerList,spinnerManager)

        getRestrictedHolidayList(shimmerFrameLayout,loutRoot,spinRestricted,alert)
        imgClose.setOnClickListener {
            alert.cancel()
            setTypeList(leaveList)
         //   binding.txtApplyingFor.text = "Applying For : 0.0"

            val spannable =
                SpannableStringBuilder("0.0")
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

        btnCancel.setOnClickListener {
            alert.cancel()
            setTypeList(leaveList)
          //  binding.txtApplyingFor.text = "Applying For : 0.0"
            val spannable =
                SpannableStringBuilder("0.0")
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
            getApplyRHLeave(rhProgress)
        })
        alert.show()
    }

    private fun getRestrictedHolidayList(
        shimmerFrameLayout: ShimmerFrameLayout,
        loutRoot: LinearLayout,
        spinRestricted: Spinner,
        alert: AlertDialog
    ) {
        Utility.setWindowNotClickable(mActivity!!.window)
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()

        val loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<RestrictedHolidayModel> = retrofitApiInterface.getRestrictedHolidayList(loginModel.access_Token)

        mService.enqueue(object : Callback<RestrictedHolidayModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<RestrictedHolidayModel>, response: Response<RestrictedHolidayModel>) {
                Utility.setWindowClickable(mActivity!!.window)
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
                                    mActivity!!,
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
                                    mActivity!!,
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
                            mActivity!!,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RestrictedHolidayModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                shimmerFrameLayout.visibility = View.GONE
                shimmerFrameLayout.stopShimmer()
            }
        })
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
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//                geocoder.getFromLocation(lat,lng,1) { address ->
//                    if (address != null)
//                        userCountry = address[0].countryName
//                }
//            } else {
//                val address = geocoder.getFromLocation(lat, lng, 1)?.get(0)
//                if (address != null)
//                    userCountry = address.countryName
//            }

            if(!userCountry.isNullOrEmpty()) {
                MyApplication.instance.setUserLocation(userCountry)
            }

            if(snackbar!=null)
            snackbar!!.dismiss()
          //  Toast.makeText(mActivity!!,"location is =>"+userCountry,Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
//            Toast.makeText(mActivity!!,e.message,Toast.LENGTH_LONG).show()

            // Utils.log("Check internet: ${e.message}")
        }
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(mActivity!!)
        locationRequest = LocationRequest().setInterval(5000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)/*.setNumUpdates(1)*/
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