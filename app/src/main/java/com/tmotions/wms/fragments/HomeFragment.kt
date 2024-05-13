package com.tmotions.wms.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tmotions.wms.R
import com.tmotions.wms.activities.*
import com.tmotions.wms.adapters.CalendarAdapter
import com.tmotions.wms.adapters.EmpLeaveAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.common.Utility.getMonthNumber
import com.tmotions.wms.common.Utility.getSendDate
import com.tmotions.wms.databinding.FragmentHomeBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.listners.NotificationReceivedListener
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.HolidayModel
import com.tmotions.wms.models.LogoutResponseModel
import com.tmotions.wms.models.dashboardModels.DashboardResponseModel
import com.tmotions.wms.models.dashboardModels.LeaveBalanceModel
import com.tmotions.wms.models.filtermodels.LeaveList
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import com.tmotions.wms.services.MyFirebaseMessagingService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class HomeFragment : Fragment(), View.OnClickListener, ItemClickListener {

    var mActivity: FragmentActivity? = null
    lateinit var binding: FragmentHomeBinding
    var month: GregorianCalendar? = null
    var itemmonth: GregorianCalendar? = null
    var items: ArrayList<String>? = null
    var sendMonthNumber = 0
    var sendYearNumber  = 0
    var event: ArrayList<String> = ArrayList()
    var startDates: ArrayList<String> = ArrayList()
    var adapter: CalendarAdapter? = null
    var handler: Handler? = null
    val arrlist_calendar = ArrayList<HolidayModel>()
    var arrLeaveBalance: ArrayList<LeaveBalanceModel> = ArrayList()
    lateinit var actionNotification : MenuItem
    lateinit var badge_text_view: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        Utility.sendViewScreenEvent("Home_Fragment_Android",mActivity!!)

        month = GregorianCalendar.getInstance() as GregorianCalendar
        itemmonth = month!!.clone() as GregorianCalendar
        items = ArrayList()
        binding.title.text = android.text.format.DateFormat.format("MMMM yyyy", month)
        sendMonthNumber = Utility.getMonth(Date())
        sendYearNumber = Utility.getYear(Date())

        binding.previous.setOnClickListener(this)
        binding.next.setOnClickListener(this)

        binding.gridview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, v, position, id ->
                (parent.adapter as CalendarAdapter).setSelected(v)
                val selectedGridDate = CalendarAdapter.dayString[position]
                val separatedTime = selectedGridDate.split("-".toRegex()).toTypedArray()
                val gridvalueString = separatedTime[2].replaceFirst(
                    "^0*".toRegex(),
                    ""
                ) // taking last part of date. ie; 2 from 2012-12-02.
                val gridvalue = gridvalueString.toInt()
                // navigate to next or previous month on clicking offdays.
                if (gridvalue > 10 && position < 8) {
                    setPreviousMonth()
                    refreshCalendar()
                } else if (gridvalue < 7 && position > 28) {
                    setNextMonth()
                    refreshCalendar()
                }
                (parent.adapter as CalendarAdapter).setSelected(v)

                val intent = Intent(mActivity, ApplyLeaveActivity::class.java)
                intent.putExtra("displayDate",Utility.setCalanderDateAfterDateSelection(selectedGridDate))
                intent.putExtra("sendDate", selectedGridDate)
                intent.putExtra("leaveType", "")
                startActivity(intent)

            }

       if(Utility.isOnline(mActivity!!)) {
                getCalenderHolidayAndLeaveList()
            }
         else
             Utility.displayAlertDialog(
                    mActivity!!,
                    resources.getString(R.string.nointernet),
                    layoutInflater
            )

       MyFirebaseMessagingService.notificationReceivedListener.setOnNotificationReceivedListener(
        object : NotificationReceivedListener.OnNotificationReceivedListener {
                override fun onNotificationReceived(args: String?) {
                    mActivity!!.runOnUiThread {
                        try{
                            var count = 0
                            if(!badge_text_view.text.toString().isEmpty())
                                count = (badge_text_view.text.toString()).toInt()

                            badge_text_view.text = (count + 1).toString()
                            badge_text_view.visibility = View.VISIBLE
                         }
                        catch (e : Exception){
                            e.printStackTrace()
                        }
                    }
                }
            })

        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.home_menu, menu)
        actionNotification = menu!!.findItem(R.id.action_notification)
        val actionView = menu.findItem(R.id.action_notification).actionView as FrameLayout
        badge_text_view = actionView.findViewById(R.id.txtNotificationCount)
        actionView.setOnClickListener {
            val intent = Intent(mActivity, NotificationActivity::class.java)
            startActivity(intent)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.action_calender -> {
                Utility.sendActionEvent("HomeFragmentCalender_Button_Android",mActivity!!)
                val intent = Intent(mActivity, ActivityCompanyHolidays::class.java)
                startActivity(intent)
                true
            }
            R.id.action_signout -> {
                if(Utility.isOnline(mActivity!!)) {
                    Utility.sendActionEvent("HomeFragmentSignout_Button_Android",mActivity!!)
                  //  logout()
                    showBottomSheetDialog()
                }
                else
                    Utility.displayAlertDialog(
                        mActivity!!,
                        resources.getString(R.string.nointernet),
                        layoutInflater
                    )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.previous -> {
                Utility.sendActionEvent("HomeFragmentCalenderPrevious_Button_Android",mActivity!!)
                if(Utility.isOnline(mActivity!!)) {
                 //   binding.mprogress.visibility = View.VISIBLE
                    setPreviousMonth()
                    refreshCalendar()
                    getCalenderHoliday()
                  //  getCalenderHolidayAndLeaveList()
                }
                else
                    Utility.displayAlertDialog(
                        mActivity!!,
                        resources.getString(R.string.nointernet),
                        layoutInflater
                    )
            }
            R.id.next -> {
                Utility.sendActionEvent("HomeFragmentCalenderNext_Button_Android",mActivity!!)
                if(Utility.isOnline(mActivity!!)) {
                    setNextMonth()
                    refreshCalendar()
                    getCalenderHoliday()
//                    getCalenderHolidayAndLeaveList()
                }
                else
                    Utility.displayAlertDialog(
                        mActivity!!,
                        resources.getString(R.string.nointernet),
                        layoutInflater
                    )
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    protected fun setPreviousMonth() {
        if (month!![GregorianCalendar.MONTH] == month!!
                .getActualMinimum(GregorianCalendar.MONTH)
        ) {
            month!![month!![GregorianCalendar.YEAR] - 1, month!!.getActualMaximum(GregorianCalendar.MONTH)] =
                1
        } else {
            month!![GregorianCalendar.MONTH] = month!![GregorianCalendar.MONTH] - 1
            //    month!![GregorianCalendar.DAY_OF_MONTH] = 19
            if (month!![GregorianCalendar.MONTH] == Utility.getMonth(Date()) - 1)
                month!![GregorianCalendar.DAY_OF_MONTH] = Utility.getDay(Date())
        }
    }

    fun refreshCalendar() {
        adapter!!.refreshDays()
        adapter!!.notifyDataSetChanged()
        handler = Handler()
        handler!!.post(calendarUpdater) // generate some calendar items
        binding.title.text = android.text.format.DateFormat.format("MMMM yyyy", month)
        var str = binding.title.text.toString().split(" ")
        sendMonthNumber = (getMonthNumber(str[0])).toInt()
        sendYearNumber = str[1].filter { !it.isWhitespace() }.toInt()
//        binding.gridview.visibility = View.VISIBLE
//        binding.shimmerFrameLayoutCalender.visibility = View.GONE
    }

    var calendarUpdater = Runnable {
        items!!.clear()

        for (i in event.indices) {
            itemmonth!!.add(GregorianCalendar.DATE, 1)
            items!!.add(startDates[i].toString())
        }
        adapter!!.setItems(items)
        adapter!!.notifyDataSetChanged()
    }

    protected fun setNextMonth() {
        if (month!![GregorianCalendar.MONTH] == month!!.getActualMaximum(GregorianCalendar.MONTH)) {
            month!![month!![GregorianCalendar.YEAR] + 1, month!!.getActualMinimum(GregorianCalendar.MONTH)] =
                1
        } else {
            month!![GregorianCalendar.MONTH] = month!![GregorianCalendar.MONTH] + 1
            if (month!![GregorianCalendar.MONTH] == Utility.getMonth(Date()) - 1)
                month!![GregorianCalendar.DAY_OF_MONTH] = Utility.getDay(Date())
        }
    }

    private fun getCalenderHolidayAndLeaveList() {
        Utility.setWindowNotClickable(mActivity!!.window)
        binding.shimmerHomeLayout.visibility = View.VISIBLE
      //  binding.shimmerFrameLayout.startShimmer()
       // binding.shimmerFrameLayout.visibility = View.VISIBLE
        var loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<DashboardResponseModel> =
            retrofitApiInterface.getCalenderHolidayAndLeaveList(
                loginModel.access_Token,
                sendYearNumber,
                sendMonthNumber
            )

        mService.enqueue(object : Callback<DashboardResponseModel> {
            override fun onResponse(
                call: Call<DashboardResponseModel>,
                response: Response<DashboardResponseModel>
            ) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val dashboardResponseModel = response.body()
                        if (dashboardResponseModel!!.statusCode == 200) {
                            binding.shimmerHomeLayout.visibility = View.GONE
                           // binding.shimmerFrameLayout.visibility = View.GONE
                          //  binding.shimmerFrameLayout.stopShimmer()
                            var count = dashboardResponseModel.data.notificationCount
                            if(count == 0){
                                badge_text_view.visibility = View.GONE
                            }
                            else {
                                badge_text_view.visibility = View.VISIBLE
                                badge_text_view.text = count.toString()
                            }
                            val leaveBalanceModel = dashboardResponseModel.data.leaveBalanceModels
                            val leaveCalenderModel = dashboardResponseModel.data.leaveCalenderModels
                            arrLeaveBalance = leaveBalanceModel

                            if (arrLeaveBalance.size > 0) {
                                val layoutManager = LinearLayoutManager(
                                    mActivity,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                                binding.recyclerEvent.layoutManager = layoutManager
                                var empLeaveAdapter = EmpLeaveAdapter(mActivity!!, leaveBalanceModel)
                                empLeaveAdapter!!.setItemClickListener(this@HomeFragment)
                                binding.recyclerEvent.adapter = empLeaveAdapter
                                binding.recyclerEvent.visibility = View.VISIBLE
                                binding.cardViewCalender.visibility = View.VISIBLE
                                binding.txtLeaveBalance.visibility = View.VISIBLE
                            }
                            else {
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    dashboardResponseModel.message,
                                    layoutInflater
                                )
                            }

                            arrlist_calendar.clear()
                            event.clear()
                            startDates.clear()

                            if (leaveCalenderModel.size > 0) {
                                for (i in 0 until leaveCalenderModel.size) {
                                    var holidayModel = HolidayModel()
                                    var leaveCalenderObj = leaveCalenderModel.get(i)
                                    var date = getSendDate(leaveCalenderObj.date)
                                    holidayModel.date = date
                                    holidayModel.count = leaveCalenderObj.noOfDays
                                    holidayModel.leaveType = leaveCalenderObj.leaveType
                                    holidayModel.leavecategory = leaveCalenderObj.leaveCategory
                                    holidayModel.leaveAbbreviation =
                                        leaveCalenderObj.leaveAbbreviation
                                    holidayModel.requestStatus = leaveCalenderObj.requestStatus
                                    if (leaveCalenderObj.noOfDays > 0) {
                                        event.add(leaveCalenderObj.noOfDays.toString())
                                        startDates.add(date)
                                    }
                                    arrlist_calendar.add(holidayModel)
                                }
                            }
                            adapter = CalendarAdapter(mActivity, month, arrlist_calendar)
                            binding.gridview.adapter = adapter

                        } else {
                            if (dashboardResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getCalenderHolidayAndLeaveList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerHomeLayout.visibility = View.GONE
                                           // binding.shimmerFrameLayout.visibility = View.GONE
                                           // binding.shimmerFrameLayout.stopShimmer()
                                        }
                                    })
                            else {
                                binding.shimmerHomeLayout.visibility = View.GONE
                              //  binding.shimmerFrameLayout.visibility = View.GONE
                              //  binding.shimmerFrameLayout.stopShimmer()
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    dashboardResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerHomeLayout.visibility = View.GONE
                       // binding.shimmerFrameLayout.visibility = View.GONE
                       // binding.shimmerFrameLayout.stopShimmer()
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

            override fun onFailure(call: Call<DashboardResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
                binding.shimmerHomeLayout.visibility = View.GONE
               // binding.shimmerFrameLayout.visibility = View.GONE
               // binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    private fun getCalenderHoliday() {
        Utility.setWindowNotClickable(mActivity!!.window)
        binding.shimmerFrameLayoutCalender.startShimmer()
        binding.shimmerFrameLayoutCalender.visibility = View.VISIBLE
        binding.gridview.visibility = View.GONE
        var loginModel = Login_Helper.getLogin(mActivity)

        val retrofitApiInterface =
            RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<DashboardResponseModel> =
            retrofitApiInterface.getCalenderHolidayAndLeaveList(
                loginModel.access_Token,
                sendYearNumber,
                sendMonthNumber
            )

        mService.enqueue(object : Callback<DashboardResponseModel> {
            override fun onResponse(
                call: Call<DashboardResponseModel>,
                response: Response<DashboardResponseModel>
            ) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val dashboardResponseModel = response.body()
                        if (dashboardResponseModel!!.statusCode == 200) {
                            binding.shimmerFrameLayoutCalender.visibility = View.GONE
                            binding.shimmerFrameLayoutCalender.stopShimmer()
                            var count = dashboardResponseModel.data.notificationCount
                            if(count == 0){
                                badge_text_view.visibility = View.GONE
                            }
                            else {
                                badge_text_view.visibility = View.VISIBLE
                                badge_text_view.text = count.toString()
                            }
                            val leaveBalanceModel = dashboardResponseModel.data.leaveBalanceModels
                            val leaveCalenderModel = dashboardResponseModel.data.leaveCalenderModels
                            arrLeaveBalance = leaveBalanceModel

                            arrlist_calendar.clear()
                            event.clear()
                            startDates.clear()

                            if (leaveCalenderModel.size > 0) {
                                for (i in 0 until leaveCalenderModel.size) {
                                    var holidayModel = HolidayModel()
                                    var leaveCalenderObj = leaveCalenderModel.get(i)
                                    var date = getSendDate(leaveCalenderObj.date)
                                    holidayModel.date = date
                                    holidayModel.count = leaveCalenderObj.noOfDays
                                    holidayModel.leaveType = leaveCalenderObj.leaveType
                                    holidayModel.leavecategory = leaveCalenderObj.leaveCategory
                                    holidayModel.leaveAbbreviation =
                                        leaveCalenderObj.leaveAbbreviation
                                    holidayModel.requestStatus = leaveCalenderObj.requestStatus
                                    if (leaveCalenderObj.noOfDays > 0) {
                                        event.add(leaveCalenderObj.noOfDays.toString())
                                        startDates.add(date)
                                    }
                                    arrlist_calendar.add(holidayModel)
                                }
                            }
                            adapter = CalendarAdapter(mActivity, month, arrlist_calendar)
                            binding.gridview.adapter = adapter
                            binding.gridview.visibility = View.VISIBLE
                            binding.shimmerFrameLayoutCalender.visibility = View.GONE
                        } else {
                            if (dashboardResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getCalenderHolidayAndLeaveList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerHomeLayout.visibility = View.GONE
                                          //  binding.shimmerFrameLayout.visibility = View.GONE
                                           // binding.shimmerFrameLayout.stopShimmer()
                                        }
                                    })
                            else {
                                binding.shimmerHomeLayout.visibility = View.GONE
                               // binding.shimmerFrameLayout.visibility = View.GONE
                              //  binding.shimmerFrameLayout.stopShimmer()
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    dashboardResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }

                    } else {
                        binding.shimmerHomeLayout.visibility = View.GONE
                      //  binding.shimmerFrameLayout.visibility = View.GONE
                      //  binding.shimmerFrameLayout.stopShimmer()
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

            override fun onFailure(call: Call<DashboardResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
                binding.shimmerHomeLayout.visibility = View.GONE
               // binding.shimmerFrameLayout.visibility = View.GONE
               // binding.shimmerFrameLayout.stopShimmer()
            }
        })
    }

    override fun onClick(view: View?, position: Int) {
        Utility.sendActionEvent("HomeFragmentLeaveRow_Button_Android",mActivity!!)
        var leaveBalanceModel = arrLeaveBalance.get(position)
        val intent = Intent(mActivity, ApplyLeaveActivity::class.java)
        intent.putExtra("leaveType", leaveBalanceModel.leaveType)
        intent.putExtra("displayDate", "")
        intent.putExtra("sendDate", "")
        intent.putExtra("holidayName", "")
        startActivity(intent)

    }

    private fun logout() {
        AlertDialog.Builder(mActivity!!, R.style.AlertDialogCustom_1)
            .setTitle("Confirm")
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                getUserSignout()
//                Login_Helper.signOut(mActivity)
//                val intent = Intent(mActivity, SplashActivity::class.java)
//                startActivity(intent)
//                mActivity!!.finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun getUserSignout() {
        Utility.setWindowNotClickable(mActivity!!.window)
        binding.mprogress.visibility = View.VISIBLE
        var loginModel = Login_Helper.getLogin(mActivity)
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LogoutResponseModel> = retrofitApiInterface.getUserLogout(loginModel.access_Token)

        mService.enqueue(object : Callback<LogoutResponseModel> {
            override fun onResponse(call: Call<LogoutResponseModel>, response: Response<LogoutResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val logoutResponseModel = response.body()
                        if (logoutResponseModel!!.statusCode == 200) {
                            Toast.makeText(mActivity!!,logoutResponseModel.message, Toast.LENGTH_LONG).show()
                            Login_Helper.signOut(mActivity)
                            MyApplication.instance.setUserLocation("")
                            MyApplication.instance.getLeaveList().clear()
                            MyApplication.instance.getLeaveSummaryTypeList().clear()
                            MyApplication.instance.getLeaveSummaryList().clear()
                            MyApplication.instance.getLeaveSummaryStatusList().clear()
                            MyApplication.instance.getOverTimeLeaveList().clear()
                            MyApplication.instance.getManagerList().clear()
                            MyApplication.instance.getSessionToList().clear()
                            MyApplication.instance.getSessionFromList().clear()
                          //  MyApplication.instance.setShortLeaveBalance(0)
                            MyApplication.instance.setManagerEmail("")
                            MyApplication.instance.setApplyingTo("")
                            val intent = Intent(mActivity, LoginActivity::class.java)
                            startActivity(intent)
                            mActivity!!.finish()
                        }  else {
                            Utility.displayAlertDialog(
                                mActivity!!,
                                logoutResponseModel.message,
                                layoutInflater
                            )
                        }
                    }
                    else {
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

            override fun onFailure(call: Call<LogoutResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(mActivity!!,R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.fragment_bottomsheet)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        val btnCancel = bottomSheetDialog.findViewById<Button>(R.id.btnCancel)
        val btnContinue = bottomSheetDialog.findViewById<Button>(R.id.btnContinue)
        btnCancel!!.setOnClickListener(View.OnClickListener {
            bottomSheetDialog.cancel()
        })
        btnContinue!!.setOnClickListener(View.OnClickListener {
            getUserSignout()
        })
//        val ll_notes = bottomSheetDialog.findViewById<LinearLayout>(R.id.ll_notes)
//        ll_notes!!.setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.show()
    }
}