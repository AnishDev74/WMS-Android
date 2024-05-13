package com.tmotions.wms.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tmotions.wms.R
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.TeamRequestAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityTeamRequestBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.filtermodels.FilterResponseModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummaryResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TeamRequest : AppCompatActivity(),View.OnClickListener, ItemClickListener, AdapterView.OnItemSelectedListener {

    lateinit var binding: ActivityTeamRequestBinding
    private var leaveList = ArrayList<LeaveList>()
    private var statusList = ArrayList<LeaveList>()
    private var resourceList = ArrayList<LeaveList>()
    private var leaveSummary = ArrayList<LeaveSummary>()

    var typeValue = ""
    var statusValue = ""
    var selectedDate = ""
    var employeeId = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_team_request)
        binding.headerLayout.txtTitle.text = "Team Request"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }
        Utility.sendViewScreenEvent("TeamRequest_Activity_Android",this)

//        binding.spinnerSelectType.setOnItemSelectedListener(this@TeamRequest)
//        binding.spinnerStatus.setOnItemSelectedListener(this@TeamRequest)
//        binding.spinnerResource.setOnItemSelectedListener(this@TeamRequest)
        binding.imgDatePicker.setOnClickListener(this@TeamRequest)
        binding.routClearFilter.setOnClickListener(this@TeamRequest)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewTeamRequest.layoutManager = layoutManager
        binding.headerLayout.imgBack.setOnClickListener(this)

        if(Utility.isOnline(this)) {
            getManagerTeamUpcomingFilter()
            getManagerTeamUpcomingLeaveSummary()
        }
        else
            Utility.displayAlertDialog(
                this,
                resources.getString(R.string.nointernet),
                layoutInflater
            )
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, p3: Long) {

        if (parent.id == R.id.spinnerSelectType){
            val typeModel = leaveList.get(position)
            if(typeModel.value.equals("All"))
                typeValue = ""
            else
            typeValue = typeModel.value
            binding.shimmerFrameLayout.visibility = View.VISIBLE
            binding.recyclerViewTeamRequest.visibility = View.GONE
            getManagerTeamUpcomingLeaveSummary()
        }

        if (parent.id == R.id.spinnerStatus){
            val statusModel = statusList.get(position)
            if(statusModel.value.equals("All"))
                statusValue = ""
            else
                statusValue = statusModel.value
            binding.shimmerFrameLayout.visibility = View.VISIBLE
            binding.recyclerViewTeamRequest.visibility = View.GONE

            getManagerTeamUpcomingLeaveSummary()

        }

        if (parent.id == R.id.spinnerResource){
            val statusModel = resourceList.get(position)
            if(statusModel.value.equals("All Resources"))
                employeeId = "0"
            else
                employeeId = statusModel.value
            binding.shimmerFrameLayout.visibility = View.VISIBLE
            binding.recyclerViewTeamRequest.visibility = View.GONE

            getManagerTeamUpcomingLeaveSummary()

        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun getManagerTeamUpcomingFilter() {
        Utility.setWindowNotClickable(window)
       val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<FilterResponseModel> = retrofitApiInterface.getManagerTeamUpcomingFilter(loginModel.access_Token)

        mService.enqueue(object : Callback<FilterResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<FilterResponseModel>, response: Response<FilterResponseModel>) {
                Utility.setWindowClickable(window)
                 try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            val data = filterResponseModel!!.data
                            leaveList = data.leaveList
                            statusList = data.statusList
                            resourceList = data.resourceList

                            if(leaveList.size > 0)
                                setTypeList(leaveList)

                            if(statusList.size > 0)
                                setStatusList(statusList)

                            if(resourceList.size > 0)
                                setResourceList(resourceList)
                         } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@TeamRequest,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getManagerTeamUpcomingFilter()
                                        }

                                        override fun onFailure() {

                                        }
                                    })
                            else {
                                Utility.displayAlertDialog(
                                    this@TeamRequest,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        Utility.displayAlertDialog(
                            this@TeamRequest,
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
                Utility.displayAlertDialog(this@TeamRequest, "$t", layoutInflater)
             }
        })
    }


    private fun setTypeList(leaveList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            this@TeamRequest,
            leaveList
        )
        binding.spinnerSelectType.adapter = dropDownAdapter
    }

    private fun setStatusList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            this@TeamRequest,
            statusList
        )
        binding.spinnerStatus.adapter = dropDownAdapter
    }

    private fun setResourceList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            this@TeamRequest,
            statusList
        )
        binding.spinnerResource.adapter = dropDownAdapter
    }

    private fun getManagerTeamUpcomingLeaveSummary() {
        Utility.setWindowNotClickable(window)
        binding.shimmerFrameLayout.visibility = View.VISIBLE

        val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveSummaryResponseModel> = retrofitApiInterface.getManagerTeamUpcomingLeaveSummary(loginModel.access_Token,typeValue,statusValue,selectedDate,employeeId)

        mService.enqueue(object : Callback<LeaveSummaryResponseModel> {
            override fun onResponse(call: Call<LeaveSummaryResponseModel>, response: Response<LeaveSummaryResponseModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        binding.spinnerSelectType.setOnItemSelectedListener(this@TeamRequest)
                        binding.spinnerStatus.setOnItemSelectedListener(this@TeamRequest)
                        binding.spinnerResource.setOnItemSelectedListener(this@TeamRequest)

                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            leaveSummary.clear()
                            val data = holidayCalenderModel!!.data
                            leaveSummary = data.leaveSummary
                            if(leaveSummary.size > 0){
                                var teamRequestAdapter = TeamRequestAdapter(this@TeamRequest, leaveSummary)
                                teamRequestAdapter.setItemClickListener(this@TeamRequest)
                                binding.recyclerViewTeamRequest.adapter = teamRequestAdapter
                                binding.recyclerViewTeamRequest.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE
                            }
                            else{
                                binding.routNoData.visibility = View.VISIBLE
                                binding.recyclerViewTeamRequest.visibility = View.GONE
                            }

                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@TeamRequest,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getManagerTeamUpcomingLeaveSummary()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@TeamRequest,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@TeamRequest,
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
                Utility.displayAlertDialog(this@TeamRequest, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.imgBack -> {
                val intent = Intent(this, ManagerViewActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.imgDatePicker -> {
                Utility.sendActionEvent("TeamRequestDatePicker_Button_Android",this)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    this,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        selectedDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        binding.txtDate.text =  Utility.displayDateFormat(dayOfMonth,monthOfYear,year)

                        binding.shimmerFrameLayout.visibility = View.VISIBLE
                        binding.recyclerViewTeamRequest.visibility = View.GONE
                        getManagerTeamUpcomingLeaveSummary()
                    },
                    year,
                    month,
                    day
                )

                dpd.show()
            }
            R.id.routClearFilter -> {
                Utility.sendActionEvent("TeamRequestClearFilter_Button_Android",this)
                typeValue = ""
                statusValue = ""
                selectedDate = ""
                employeeId = "0"
                binding.txtDate.text = "dd/mm/yyyy"
                setTypeList(leaveList)
                setStatusList(statusList)
                setResourceList(resourceList)
                getManagerTeamUpcomingLeaveSummary()

            }
        }
    }

    override fun onClick(view: View?, position: Int) {
        Utility.sendActionEvent("TeamRequestRow_Button_Android",this)
        var leaveSummaryModel = leaveSummary.get(position)
        val intent = Intent(this, ManagerLeaveSummaryDetailsActivity::class.java)
        intent.putExtra("guid",leaveSummaryModel.guid)
      //  intent.putExtra("status",leaveSummaryModel.LeaveStatus)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ManagerViewActivity::class.java)
        startActivity(intent)
        finish()
    }
}