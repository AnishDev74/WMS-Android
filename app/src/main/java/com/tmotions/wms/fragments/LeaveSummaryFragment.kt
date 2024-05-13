package com.tmotions.wms.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tmotions.wms.R
import com.tmotions.wms.activities.ActivityLeaveSummaryDetails
import com.tmotions.wms.adapters.DropDownAdapter
import com.tmotions.wms.adapters.LeaveSummaryAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.DividerItemDecorator
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.FragmentHomeBinding
import com.tmotions.wms.databinding.FragmentLeaveSummaryBinding
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

class LeaveSummaryFragment : Fragment() ,View.OnClickListener, ItemClickListener, AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentLeaveSummaryBinding
    private var leaveList = ArrayList<LeaveList>()
    private var statusList = ArrayList<LeaveList>()
    private var leaveSummary = ArrayList<LeaveSummary>()
    private var typeValue = ""
    private var statusValue = ""
    private var selectedDate = ""
    var mActivity: FragmentActivity? = null
    
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_leave_summary, container, false)

        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewLeaveSummary.layoutManager = layoutManager
        Utility.sendViewScreenEvent("LeaveSummary_Fragment_Android",mActivity!!)

        val itemDecoration2 = DividerItemDecorator(
            resources.getDrawable(
                R.drawable.review_divider,
                null
            )
        )
        binding.recyclerViewLeaveSummary.addItemDecoration(itemDecoration2)

       /* if(MyApplication.instance.getLeaveSummaryTypeList().size > 0){
            leaveList = MyApplication.instance.getLeaveSummaryTypeList()
            statusList = MyApplication.instance.getLeaveSummaryStatusList()
            setTypeList(leaveList)
            setStatusList(statusList)

            if(MyApplication.instance.getLeaveSummaryList().size > 0){
                binding.shimmerFrameLayout.visibility = View.GONE
                leaveSummary = MyApplication.instance.getLeaveSummaryList()
                var upcomingRequestAdapter = LeaveSummaryAdapter(mActivity!!, leaveSummary)
                upcomingRequestAdapter!!.setItemClickListener(this@LeaveSummaryFragment)
                binding.recyclerViewLeaveSummary.adapter = upcomingRequestAdapter
                binding.refreshLayout.visibility = View.VISIBLE
                binding.routNoData.visibility = View.GONE
            }
            else{
                binding.routNoData.visibility = View.VISIBLE
                binding.refreshLayout.visibility = View.GONE
            }

            binding.spinnerSelectType.onItemSelectedListener = this@LeaveSummaryFragment
            binding.spinnerStatus.onItemSelectedListener = this@LeaveSummaryFragment
            binding.imgDatePicker.setOnClickListener(this@LeaveSummaryFragment)
            binding.routClearFilter.setOnClickListener(this@LeaveSummaryFragment)
        }*/
       // else {
            if (Utility.isOnline(mActivity!!)) {
                if(MyApplication.instance.getLeaveSummaryTypeList().size > 0) {
                    leaveList = MyApplication.instance.getLeaveSummaryTypeList()
                    statusList = MyApplication.instance.getLeaveSummaryStatusList()
                    setTypeList(leaveList)
                    setStatusList(statusList)
                    binding.spinnerSelectType.onItemSelectedListener = this@LeaveSummaryFragment
                    binding.spinnerStatus.onItemSelectedListener = this@LeaveSummaryFragment
                    binding.imgDatePicker.setOnClickListener(this@LeaveSummaryFragment)
                    binding.routClearFilter.setOnClickListener(this@LeaveSummaryFragment)
                }else
                getLeaveSummaryFilter()

//                if(MyApplication.instance.getLeaveSummaryList().size > 0){
//                    binding.shimmerFrameLayout.visibility = View.GONE
//                    binding.refreshLayout.visibility = View.VISIBLE
//                    binding.routNoData.visibility = View.GONE
//                    leaveSummary = MyApplication.instance.getLeaveSummaryList()
//                    var upcomingRequestAdapter = LeaveSummaryAdapter(mActivity!!, leaveSummary)
//                    upcomingRequestAdapter!!.setItemClickListener(this@LeaveSummaryFragment)
//                    binding.recyclerViewLeaveSummary.adapter = upcomingRequestAdapter
//
//                }
//                else{
//                    getLeaveSummaryList()
//                }
                getLeaveSummaryList()
            } else
                Utility.displayAlertDialog(
                    mActivity!!,
                    resources.getString(R.string.nointernet),
                    layoutInflater
                )
     //   }

        binding.refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // implement Handler to wait for 3 seconds and then update UI means update value of TextView
            Handler(Looper.myLooper()!!).postDelayed(Runnable { // cancle the Visual indication of a refresh
//                binding.refreshLayout.setRefreshing(false)
                val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
                binding.recyclerViewLeaveSummary.layoutManager = layoutManager

                val itemDecoration2 = DividerItemDecorator(
                    resources.getDrawable(
                        R.drawable.review_divider,
                        null
                    )
                )
                binding.recyclerViewLeaveSummary.addItemDecoration(itemDecoration2)
                getLeaveSummaryList()
            }, 3000)
        })

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
    }

    override fun onClick(view: View?, position: Int) {

        var leaveSummaryModel = leaveSummary.get(position)
        val intent = Intent(mActivity, ActivityLeaveSummaryDetails::class.java)
        intent.putExtra("guid",leaveSummaryModel.guid)
     //   intent.putExtra("status",leaveSummaryModel.LeaveStatus)
        intent.putExtra("noficationGuid","")
        startActivity(intent)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgDatePicker -> {
                Utility.sendActionEvent("LeaveSummaryFragmentFromDate_Button_Android",mActivity!!)
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    mActivity!!,
                    R.style.datepicker,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        selectedDate = Utility.sendDateFormat(dayOfMonth,monthOfYear,year)
                        binding.txtFromDate.text =  Utility.displayDateFormat(dayOfMonth,monthOfYear,year)

                        binding.shimmerFrameLayout.visibility = View.VISIBLE
                        binding.refreshLayout.visibility = View.GONE
                        getLeaveSummaryList()
                    },
                    year,
                    month,
                    day
                )

                dpd.show()
            }
            R.id.routClearFilter -> {
                Utility.sendActionEvent("LeaveSummaryFragmentClear_Button_Android",mActivity!!)
                if(Utility.isOnline(mActivity!!)) {
                    typeValue = ""
                    statusValue = ""
                    selectedDate = ""
                    binding.txtFromDate.text = "dd/mm/yyyy"
                    setTypeList(leaveList)
                    setStatusList(statusList)
                    getLeaveSummaryFilter()
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
    override fun onItemSelected(parent: AdapterView<*>, p1: View?, position: Int, p3: Long) {

        if (parent.id == R.id.spinnerSelectType){
            val typeModel = leaveList.get(position)
            if(typeModel.value.equals("All"))
                typeValue = ""
            else
                typeValue = typeModel.value
            binding.shimmerFrameLayout.visibility = View.VISIBLE
            binding.refreshLayout.visibility = View.GONE

            getLeaveSummaryList()
        }

        if (parent.id == R.id.spinnerStatus){
            val statusModel = statusList.get(position)
            if(statusModel.value.equals("All"))
                statusValue = ""
            else
                statusValue = statusModel.value
            binding.shimmerFrameLayout.visibility = View.VISIBLE
            binding.refreshLayout.visibility = View.GONE
            getLeaveSummaryList()
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun getLeaveSummaryFilter() {
        Utility.setWindowNotClickable(mActivity!!.window)
        val loginModel = Login_Helper.getLogin(mActivity)
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<FilterResponseModel> = retrofitApiInterface.getLeaveSummaryFilter(loginModel.access_Token)

        mService.enqueue(object : Callback<FilterResponseModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<FilterResponseModel>, response: Response<FilterResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                try {
                    if (response.isSuccessful) {
                        val filterResponseModel = response.body()
                        if (filterResponseModel!!.statusCode == 200) {
                            val data = filterResponseModel!!.data
                            leaveList = data.leaveList
                            statusList = data.statusList

                            if(leaveList.size > 0)
                                setTypeList(leaveList)

                            if(statusList.size > 0)
                                setStatusList(statusList)

                            MyApplication.instance.setLeaveSummaryTypeList(leaveList)
                            MyApplication.instance.setLeaveSummaryStatusList(statusList)


                            binding.spinnerSelectType.setOnItemSelectedListener(this@LeaveSummaryFragment)
                            binding.spinnerStatus.setOnItemSelectedListener(this@LeaveSummaryFragment)
                            binding.imgDatePicker.setOnClickListener(this@LeaveSummaryFragment)
                            binding.routClearFilter.setOnClickListener(this@LeaveSummaryFragment)

                        } else {
                            if (filterResponseModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveSummaryFilter()
                                        }

                                        override fun onFailure() {

                                        }
                                    })
                            else {
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    filterResponseModel.message,
                                    layoutInflater
                                )
                            }
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

            override fun onFailure(call: Call<FilterResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
           }
        })
    }

    private fun setTypeList(leaveList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            leaveList
        )
        binding.spinnerSelectType.adapter = dropDownAdapter
    }

    private fun setStatusList(statusList: ArrayList<LeaveList>) {
        val dropDownAdapter = DropDownAdapter(
            mActivity!!,
            statusList
        )
        binding.spinnerStatus.adapter = dropDownAdapter
    }

    private fun getLeaveSummaryList() {
        Utility.setWindowNotClickable(mActivity!!.window)
        val loginModel = Login_Helper.getLogin(mActivity)
        binding.shimmerFrameLayout.visibility = View.VISIBLE

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<LeaveSummaryResponseModel> = retrofitApiInterface.getLeaveSummaryList(loginModel.access_Token,typeValue,statusValue,selectedDate)

        mService.enqueue(object : Callback<LeaveSummaryResponseModel> {
            override fun onResponse(call: Call<LeaveSummaryResponseModel>, response: Response<LeaveSummaryResponseModel>) {
                Utility.setWindowClickable(mActivity!!.window)
                binding.refreshLayout.setRefreshing(false)
                try {
                    if (response.isSuccessful) {
                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            val data = holidayCalenderModel!!.data
                            leaveSummary = data.leaveSummary
                            MyApplication.instance.setLeaveSummaryList(leaveSummary)
                            if(leaveSummary.size > 0){
                                var upcomingRequestAdapter = LeaveSummaryAdapter(mActivity!!, leaveSummary)
                                upcomingRequestAdapter!!.setItemClickListener(this@LeaveSummaryFragment)
                                binding.recyclerViewLeaveSummary.adapter = upcomingRequestAdapter
                                binding.refreshLayout.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE
                            }
                            else{
                                binding.routNoData.visibility = View.VISIBLE
                                binding.refreshLayout.visibility = View.GONE
                            }

                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    mActivity!!,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getLeaveSummaryList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    mActivity!!,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
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

            override fun onFailure(call: Call<LeaveSummaryResponseModel>, t: Throwable) {
                Utility.setWindowClickable(mActivity!!.window)
                Utility.displayAlertDialog(mActivity!!, "$t", layoutInflater)
                binding.refreshLayout.setRefreshing(false)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }
}