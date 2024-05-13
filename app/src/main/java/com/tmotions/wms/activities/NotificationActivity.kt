package com.tmotions.wms.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tmotions.wms.R
import com.tmotions.wms.adapters.NotificationAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.DividerItemDecorator
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityNotificationBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import com.tmotions.wms.models.notificationmodels.NotificationList
import com.tmotions.wms.models.notificationmodels.NotificationResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NotificationActivity : AppCompatActivity(),View.OnClickListener, ItemClickListener {

    lateinit var binding: ActivityNotificationBinding
    private var notificationList = ArrayList<NotificationList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_notification)
        binding.headerLayout.txtTitle.text = "Notification"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }
        Utility.sendViewScreenEvent("Notification_Activity_Android",this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val itemDecoration2 =
            DividerItemDecorator(resources.getDrawable(R.drawable.review_divider, null))
        binding.recyclerViewNotification.addItemDecoration(itemDecoration2)
        binding.recyclerViewNotification.layoutManager = layoutManager

        binding.headerLayout.imgBack.setOnClickListener(this)
        getNotificationList()

    }

    private fun getNotificationList() {
        Utility.setWindowNotClickable(window)
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.recyclerViewNotification.visibility = View.GONE
        binding.routNoData.visibility = View.GONE

        val loginModel = Login_Helper.getLogin(this)

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<NotificationResponseModel> = retrofitApiInterface.getNotificationList(loginModel.access_Token)

        mService.enqueue(object : Callback<NotificationResponseModel> {
            override fun onResponse(call: Call<NotificationResponseModel>, response: Response<NotificationResponseModel>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val notificationResponseModel = response.body()
                        if (notificationResponseModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            val data = notificationResponseModel!!.data
                            notificationList = data.notificationList
                            if(notificationList.size > 0){
                                binding.recyclerViewNotification.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE

                                var notificationAdapter = NotificationAdapter(this@NotificationActivity, data.notificationList)
                                notificationAdapter.setItemClickListener(this@NotificationActivity)
                                binding.recyclerViewNotification.adapter = notificationAdapter

                            }
                            else{
                                binding.routNoData.visibility = View.VISIBLE
                                binding.recyclerViewNotification.visibility = View.GONE
                            }

                        } else {
                            if (notificationResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@NotificationActivity,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getNotificationList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@NotificationActivity,
                                    notificationResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@NotificationActivity,
                            "${response.message()}",
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<NotificationResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@NotificationActivity, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }

    override fun onClick(view: View?, position: Int) {
        Utility.sendActionEvent("NotificationRowClick_Button_Android",this)

        var notificationListModel = notificationList.get(position)
        if(notificationListModel.IsManagerView){
            val intent = Intent(this, ManagerLeaveSummaryDetailsActivity::class.java)
            intent.putExtra("guid",notificationListModel.leave_Guid)
            intent.putExtra("noficationGuid",notificationListModel.notificationGuid)
            startActivity(intent)
        }
        else{
            val intent = Intent(this, ActivityLeaveSummaryDetails::class.java)
            intent.putExtra("guid",notificationListModel.leave_Guid)
            intent.putExtra("noficationGuid",notificationListModel.notificationGuid)
            startActivity(intent)
        }

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.imgBack -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}