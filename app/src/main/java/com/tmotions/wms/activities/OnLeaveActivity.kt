package com.tmotions.wms.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tmotions.wms.R
import com.tmotions.wms.adapters.OnLeaveAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityOnLeaveBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.OnLeaveModel
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummaryResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class OnLeaveActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityOnLeaveBinding
    private var calendar: Calendar? = null
    var typeValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_on_leave)
        binding.headerLayout.txtTitle.text = "On Leave"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }

        Utility.sendViewScreenEvent("OnLeave_Activity_Android",this)
        calendar = Calendar.getInstance()

        var layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewOnLeave.layoutManager = layoutManager

        binding.imgLeft.setOnClickListener(this)
        binding.imgRight.setOnClickListener(this)
        binding.headerLayout.imgBack.setOnClickListener(this)

        updateTextView()

        val sendDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        typeValue = sendDateFormat.format(calendar!!.time)
        if(Utility.isOnline(this)) {
            getManagerLeaveSummaryList()
        }
        else
            Utility.displayAlertDialog(
                this,
                resources.getString(R.string.nointernet),
                layoutInflater
            )
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.imgLeft -> {
                Utility.sendActionEvent("OnLeaveCalenderLeft_Button_Android",this)
                calendar!!.add(Calendar.DATE, -1);
                updateTextView()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                typeValue = dateFormat.format(calendar!!.time)
                getManagerLeaveSummaryList()
            }
            R.id.imgRight -> {
                Utility.sendActionEvent("OnLeaveCalenderRight_Button_Android",this)
                calendar!!.add(Calendar.DATE, 1);
                updateTextView()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                typeValue = dateFormat.format(calendar!!.time)
                getManagerLeaveSummaryList()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }
    
    private fun updateTextView() {
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val formattedDate: String = dateFormat.format(calendar!!.time)
        binding.txtDate.text = formattedDate

    }

    private fun getManagerLeaveSummaryList() {
        Utility.setWindowNotClickable(window)
        binding.imgLeft.isEnabled = false
        binding.imgRight.isEnabled = false
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.recyclerViewOnLeave.visibility = View.GONE
        binding.routNoData.visibility = View.GONE
        val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveSummaryResponseModel> = retrofitApiInterface.getLeaveSummaryByDate(loginModel.access_Token,typeValue)

        mService.enqueue(object : Callback<LeaveSummaryResponseModel> {
            override fun onResponse(call: Call<LeaveSummaryResponseModel>, response: Response<LeaveSummaryResponseModel>) {
                Utility.setWindowClickable(window)
                binding.imgLeft.isEnabled = true
                binding.imgRight.isEnabled = true
                try {
                    if (response.isSuccessful) {
                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            val data = holidayCalenderModel!!.data
                            if(data.leaveSummary.size > 0){
                                binding.recyclerViewOnLeave.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE

                                var leaveBalanceAdapter = OnLeaveAdapter(this@OnLeaveActivity, data.leaveSummary)
                                binding.recyclerViewOnLeave.adapter = leaveBalanceAdapter

                            }
                            else{
                                binding.routNoData.visibility = View.VISIBLE
                                binding.recyclerViewOnLeave.visibility = View.GONE
                            }

                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@OnLeaveActivity,
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
                                    this@OnLeaveActivity,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@OnLeaveActivity,
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
                Utility.displayAlertDialog(this@OnLeaveActivity, "$t", layoutInflater)
                binding.imgLeft.isEnabled = true
                binding.imgRight.isEnabled = true
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }
}