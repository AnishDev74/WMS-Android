package com.tmotions.wms.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.tmotions.wms.R
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.ManagerResourceListDropDownAdapter
import com.tmotions.wms.adapters.ManagerViewDropDownAdapter
import com.tmotions.wms.adapters.ManagerViewLeaveSummaryAdapter
import com.tmotions.wms.adapters.ResourceListDropDownAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityManagerViewBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.ItemCheckedListener
import com.tmotions.wms.listners.ManagerViewItemClickListener
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LeaveApplyResponseModel
import com.tmotions.wms.models.LeaveRequestModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummaryResponseModel
import com.tmotions.wms.models.managerview.Data
import com.tmotions.wms.models.managerview.LeaveTypesList
import com.tmotions.wms.models.managerview.ManagerFilterResponseModel
import com.tmotions.wms.models.managerview.ResourceList
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class ManagerViewActivity : AppCompatActivity(), View.OnClickListener, ManagerViewItemClickListener,ItemCheckedListener {

    lateinit var binding: ActivityManagerViewBinding
    private var leaveSummary = ArrayList<LeaveSummary>()
    private var leaveList = ArrayList<LeaveTypesList>()
    private var typeValue = ""
    private var cr50a_employeeid = "0"
    var snackbar: Snackbar? = null
/*    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest*/
   // private var arrayListCheckItemGuid = ArrayList<Int>()
    private var arrayListCheckItemGuid = ArrayList<String>()
    private var managerViewLeaveSummaryAdapter: ManagerViewLeaveSummaryAdapter? = null
    private var resourceList = ArrayList<ResourceList>()

   /* companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    var userCountry = ""*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_view)

        MyApplication.instance.activity = this

        binding.txtManagerHome.text = "Pending Requests"
        Utility.sendViewScreenEvent("ManagerViewPendingRequests_Activity_Android", this)

        var layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewItems.layoutManager = layoutManager

        binding.txtOnLeave.setOnClickListener(this)
        binding.txtTeamRequest.setOnClickListener(this)
        binding.txtEmployeeView.setOnClickListener(this)
        binding.txtEmpWFH.setOnClickListener(this)
        binding.txtApprove.setOnClickListener(this)
        binding.txtTypes.setOnClickListener(this)
        binding.txtResource.setOnClickListener(this)

        if (Utility.isOnline(this)) {
            getManagerLeaveSummaryList()
            getResourceAndLeaveTypeList()
        } else
            Utility.displayAlertDialog(
                this,
                resources.getString(R.string.nointernet),
                layoutInflater
            )

        binding.refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // implement Handler to wait for 3 seconds and then update UI means update value of TextView
            Handler(Looper.myLooper()!!).postDelayed(Runnable { // cancle the Visual indication of a refresh
//                binding.refreshLayout.setRefreshing(false)
                var layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                binding.recyclerViewItems.layoutManager = layoutManager
                binding.chkboxSelectAll.isChecked = false
                binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
                getManagerLeaveSummaryList()
            }, 3000)
        })

        binding.chkboxSelectAll.setOnClickListener(View.OnClickListener {
            arrayListCheckItemGuid.clear()
            if (binding.chkboxSelectAll.isChecked) {
                leaveSummary = getModel(true)
                managerViewLeaveSummaryAdapter = ManagerViewLeaveSummaryAdapter(this, leaveSummary,true)
                managerViewLeaveSummaryAdapter!!.setItemCheckedListener(this@ManagerViewActivity)
                managerViewLeaveSummaryAdapter!!.setItemClickListener(this@ManagerViewActivity)
                binding.recyclerViewItems.adapter = managerViewLeaveSummaryAdapter
                //  binding.txtApprove.isClickable = true
                  binding.txtApprove.setBackgroundResource(R.drawable.btn_age_yes)
                  binding.txtApprove.isClickable = true
            } else {
                leaveSummary = getModel(false)
                managerViewLeaveSummaryAdapter = ManagerViewLeaveSummaryAdapter(this, leaveSummary,false)
                managerViewLeaveSummaryAdapter!!.setItemCheckedListener(this@ManagerViewActivity)
                managerViewLeaveSummaryAdapter!!.setItemClickListener(this@ManagerViewActivity)
                binding.recyclerViewItems.adapter = managerViewLeaveSummaryAdapter
                //   binding.txtApprove.isClickable = false
                binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
                binding.txtApprove.isClickable = false
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.txtOnLeave -> {
                Utility.sendActionEvent("OnLeave_Button_Android", this)
                val intent = Intent(this, OnLeaveActivity::class.java)
                startActivity(intent)
            }

            R.id.txtTeamRequest -> {
                Utility.sendActionEvent("TeamRequest_Button_Android", this)
                val intent = Intent(this, TeamRequest::class.java)
                startActivity(intent)
                finish()
            }

            R.id.txtEmployeeView -> {
                Utility.sendActionEvent("Employee5View_Button_Android", this)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.txtEmpWFH -> {
                Utility.sendActionEvent("EmpWfh_Button_Android", this)
                val intent = Intent(this, EmpWFHActivity::class.java)
                startActivity(intent)
                // finish()
            }

            R.id.txtApprove -> {
                if (arrayListCheckItemGuid.size > 0)
                    openSelectAllDialog()
              }

            R.id.txtTypes -> {
                showLeavePopup(v)
            }

            R.id.txtResource -> {
                showReSourcePopup(v)
            }

        }
    }

    private fun getResourceAndLeaveTypeList() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<ManagerFilterResponseModel> =
            retrofitApiInterface.getResourceAndLeaveTypeList(loginModel.access_Token)

        mService.enqueue(object : Callback<ManagerFilterResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<ManagerFilterResponseModel>,
                response: Response<ManagerFilterResponseModel>
            ) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            val data = filterResponseModel!!.data
                            leaveList = data.leaveTypesList
                            resourceList = data.resourceList
                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerViewActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getResourceAndLeaveTypeList()
                                        }

                                        override fun onFailure() {
                                        }
                                    })
                            else {
                                Utility.displayAlertDialog(
                                    this@ManagerViewActivity,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    } else {
                        Utility.displayAlertDialog(
                            this@ManagerViewActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ManagerFilterResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ManagerViewActivity, "$t", layoutInflater)
            }
        })
    }


    private fun getManagerLeaveSummaryList() {
        Utility.setWindowNotClickable(window)
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.refreshLayout.visibility = View.GONE
        binding.routNoData.visibility = View.GONE

        val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<LeaveSummaryResponseModel> =
            retrofitApiInterface.getManagerLeaveSummaryList(
                loginModel.access_Token,
                binding.txtTypes.text.toString(),
                cr50a_employeeid
            )

        mService.enqueue(object : Callback<LeaveSummaryResponseModel> {
            override fun onResponse(
                call: Call<LeaveSummaryResponseModel>,
                response: Response<LeaveSummaryResponseModel>
            ) {
                Utility.setWindowClickable(window)
                binding.refreshLayout.setRefreshing(false)
                try {
                    if (response.isSuccessful) {
                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            val data = holidayCalenderModel!!.data
                            leaveSummary = data.leaveSummary
                            arrayListCheckItemGuid.clear()

                            if (leaveSummary.size > 0) {
                                managerViewLeaveSummaryAdapter = ManagerViewLeaveSummaryAdapter(
                                    this@ManagerViewActivity,
                                    leaveSummary,
                                    false
                                )
                                managerViewLeaveSummaryAdapter!!.setItemClickListener(this@ManagerViewActivity)
                                managerViewLeaveSummaryAdapter!!.setItemCheckedListener(this@ManagerViewActivity)
                                binding.recyclerViewItems.adapter = managerViewLeaveSummaryAdapter
                                binding.refreshLayout.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE
                                binding.chkboxSelectAll.isClickable = true
                            } else {
                                binding.routNoData.visibility = View.VISIBLE
                                binding.refreshLayout.visibility = View.GONE
                                binding.chkboxSelectAll.isClickable = false
                                binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
                                binding.txtApprove.isClickable = false
                            }

                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerViewActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getManagerLeaveSummaryList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ManagerViewActivity,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    } else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ManagerViewActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<LeaveSummaryResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ManagerViewActivity, "$t", layoutInflater)
                binding.refreshLayout.setRefreshing(false)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onClick(type: String?, position: Int) {

        var leaveSummary = leaveSummary.get(position)
        var status = leaveSummary.leaveStatus
        /*
        if (userCountry.isNullOrEmpty()) {
            Utility.displayAlertDialog(this, "User location not found ", layoutInflater)
        }
        else {
            if (type.equals("Approved")) {
                getChangeLeaveStatus(
                    "Approved", "", leaveSummary.guid
                )
            } else if (type.equals("Cancelled")) {
                if (status.equals("Pending"))
                    reasonDialog(leaveSummary.guid, "Disapproved")
                else
                    reasonDialog(leaveSummary.guid, "Rejected")
            } else {
                val intent = Intent(this, ManagerLeaveSummaryDetailsActivity::class.java)
                intent.putExtra("guid", leaveSummary.guid)
                startActivity(intent)
                //  finish()
            }
        }*/
        if (type.equals("Approved")) {
            getChangeLeaveStatus(
                "Approved", "", leaveSummary.guid,true)
        } else if (type.equals("Cancelled")) {
            if (status.equals("Pending"))
                reasonDialog(leaveSummary.guid, "Disapproved")
            else
                reasonDialog(leaveSummary.guid, "Rejected")
        } else {
            val intent = Intent(this, ManagerLeaveSummaryDetailsActivity::class.java)
            intent.putExtra("guid", leaveSummary.guid)
            startActivity(intent)
            //  finish()
        }
    }

    private fun getChangeLeaveStatus(leaveStatus: String, reason: String, guid: String,isApproved:Boolean) {
        Utility.setWindowNotClickable(window)
        binding.mprogress.visibility = View.VISIBLE
        var leaveRequestModel = LeaveRequestModel()

        leaveRequestModel.guid = guid
        leaveRequestModel.status = leaveStatus
        leaveRequestModel.reason = reason
        //   leaveRequestModel.timeZone = userCountry

        val loginModel = Login_Helper.getLogin(this@ManagerViewActivity)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<LeaveApplyResponseModel> =
            retrofitApiInterface.getChangeLeaveStatus(loginModel.access_Token, leaveRequestModel)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<LeaveApplyResponseModel>,
                response: Response<LeaveApplyResponseModel>
            ) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            if(isApproved)
                            Utility.playBeep(this@ManagerViewActivity)
                            Toast.makeText(
                                this@ManagerViewActivity, baseResponseModel.message,
                                Toast.LENGTH_LONG
                            ).show()

                            getManagerLeaveSummaryList()

                        } else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerViewActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getChangeLeaveStatus(
                                                leaveStatus,
                                                reason,
                                                guid,isApproved
                                            )
                                        }

                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ManagerViewActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    } else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ManagerViewActivity,
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
                Utility.displayAlertDialog(this@ManagerViewActivity, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun reasonDialog(guid: String, leaveStatus: String) {
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
                    getChangeLeaveStatus(leaveStatus, edtReason.text.toString(), guid,false)
                } else
                    Toasty.error(this@ManagerViewActivity, resources.getString(R.string.nointernet))
                        .show()
            }

        }
        btnCancel.setOnClickListener {
            alert.cancel()

        }

        alert.show()
    }


    // location implementation start
/*    override fun onStart() {
        super.onStart()
        if (!MyApplication.instance.getUserLocation().isNullOrEmpty()) {
            userCountry = MyApplication.instance.getUserLocation()
        } else {
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
                                this@ManagerViewActivity,
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
            if (snackbar != null)
                snackbar!!.dismiss()

        } catch (e: IOException) {
            // Utils.log("Check internet: ${e.message}")
        }
    }


    private fun setUpLocationListener() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest().setInterval(1000).setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

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
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
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

    override fun onChecked(position: Int, ischecked: Boolean) {
        var model = leaveSummary.get(position)
        if (ischecked) {
            arrayListCheckItemGuid.add(model.guid)
            binding.txtApprove.setBackgroundResource(R.drawable.btn_age_yes)
            if (arrayListCheckItemGuid.size == leaveSummary.size) {
                binding.chkboxSelectAll.isChecked = true
                binding.txtApprove.isClickable = true
            }
            else
                binding.txtApprove.isClickable = true
        } else {
            arrayListCheckItemGuid.remove(model.guid)
            if(arrayListCheckItemGuid.size == 0) {
                binding.chkboxSelectAll.isChecked = false
                binding.txtApprove.isClickable = false
                binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
            }
            else
                binding.chkboxSelectAll.isChecked = false
        }
    }

    private fun getModel(isSelect: Boolean): ArrayList<LeaveSummary> {
        val list: ArrayList<LeaveSummary> = ArrayList()
        for (i in 0 until leaveSummary.size) {
            val leaveSummary1 = LeaveSummary()
            leaveSummary1.isChecked = isSelect
            leaveSummary1.employeeId = leaveSummary[i].employeeId
            leaveSummary1.employeeName = leaveSummary[i].employeeName
            leaveSummary1.leaveType = leaveSummary[i].leaveType
            leaveSummary1.leaveDay = leaveSummary[i].leaveDay
            leaveSummary1.leaveStatus = leaveSummary[i].leaveStatus
            leaveSummary1.leaveCategory = leaveSummary[i].leaveCategory
            leaveSummary1.guid = leaveSummary[i].guid
            list.add(leaveSummary1)

            if (isSelect) {
                //  if (!arrayListCheckItemGuid.contains(leaveSummary[i].employeeId))
                arrayListCheckItemGuid.add(leaveSummary[i].guid)
            } else {
                arrayListCheckItemGuid.clear()
            }
        }
        return list
    }

    private fun openSelectAllDialog() {
        // Create and show a custom dialog here
        val dialog = Dialog(this@ManagerViewActivity)
        dialog.setContentView(R.layout.approve_all_pop_up)
        // Set the dialog window size to wrap its content
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        // layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
//  layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        dialog.window?.attributes = layoutParams

        // Set the custom background with rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_border)

        // Prevent closing when tapping outside the dialog
        dialog.setCanceledOnTouchOutside(false)

        // Get a reference to the cancel button
        val closeButton = dialog.findViewById<Button>(R.id.dialogClose_button)
        val approveButton = dialog.findViewById<Button>(R.id.dialogApprove_button)
        val txtSelectRequest = dialog.findViewById<TextView>(R.id.txtSelectRequest)
        txtSelectRequest.text = "Selected Request :" + arrayListCheckItemGuid.size

        // Set an OnClickListener for the cancel button
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        approveButton.setOnClickListener {
            dialog.dismiss()
            getChangeLeaveStatusInBulk(arrayListCheckItemGuid)
        }

        dialog.show()
    }

    private fun getChangeLeaveStatusInBulk(employeeGuidList: ArrayList<String>) {
        binding.mprogress.visibility = View.VISIBLE
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@ManagerViewActivity)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveApplyResponseModel> = retrofitApiInterface.getChangeLeaveStatusInBulk(loginModel.access_Token,employeeGuidList)

        mService.enqueue(object : Callback<LeaveApplyResponseModel> {
            override fun onResponse(call: Call<LeaveApplyResponseModel>, response: Response<LeaveApplyResponseModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            binding.mprogress.visibility = View.GONE
                            binding.chkboxSelectAll.isChecked = false
                            Utility.playBeep(this@ManagerViewActivity)
                            Toast.makeText(this@ManagerViewActivity,baseResponseModel.message, Toast.LENGTH_LONG).show()
                            cr50a_employeeid = "0"
                            binding.txtTypes.text = "All"
                            binding.txtResource.text = "All"
                            getManagerLeaveSummaryList()
                        }
                        else {
                            if (baseResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ManagerViewActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getChangeLeaveStatusInBulk(employeeGuidList)
                                        }
                                        override fun onFailure() {
                                            binding.mprogress.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.mprogress.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ManagerViewActivity,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.mprogress.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ManagerViewActivity,
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
                Utility.displayAlertDialog(this@ManagerViewActivity, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun showLeavePopup(anchorView: View) {
        val popup = ListPopupWindow(this)
        val dropDownAdapter = ManagerViewDropDownAdapter(
            this,
            leaveList
        )
        popup.setAdapter(dropDownAdapter)
        popup.anchorView = anchorView
        popup.verticalOffset = -120
        popup.setOnItemClickListener { parent, view, position, id ->
            binding.txtTypes.text = leaveList.get(position).text
            binding.chkboxSelectAll.isChecked = false
            binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
            binding.txtApprove.isClickable = false
            getManagerLeaveSummaryList()
            popup.dismiss()
        }
        popup.show()
    }

    private fun showReSourcePopup(anchorView: View) {
        val popup = ListPopupWindow(this)
        val dropDownAdapter = ManagerResourceListDropDownAdapter(
            this,
            resourceList
        )
        popup.setAdapter(dropDownAdapter)
        popup.anchorView = anchorView
        popup.verticalOffset = -120
        popup.setOnItemClickListener { parent, view, position, id ->
            binding.txtResource.text = resourceList.get(position).tm_employee_name
            cr50a_employeeid =  resourceList.get(position).cr50a_employeeid
            binding.chkboxSelectAll.isChecked = false
            binding.txtApprove.setBackgroundResource(R.drawable.gray_border)
            binding.txtApprove.isClickable = false
            getManagerLeaveSummaryList()
            popup.dismiss()
        }
        popup.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
