package com.tmotions.wms.activities

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.tmotions.wms.R
import com.tmotions.wms.adapters.CalendarAdapter
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityHomeBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.fragments.*
import com.tmotions.wms.models.HolidayModel
import com.tmotions.wms.models.LoginModel
import java.util.*


class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var homeBinding: ActivityHomeBinding
    lateinit var toolbar: Toolbar
    lateinit var loginmodel : LoginModel
    var key = ""
    var month: GregorianCalendar? = null
    var itemmonth: GregorianCalendar? = null
    val arrlist_calendar = ArrayList<HolidayModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = homeBinding.headerLayout.toolbar
        setSupportActionBar(toolbar)
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
         //   window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }
        Utility.sendViewScreenEvent("Home_Activity_Android",this)
        month = GregorianCalendar.getInstance() as GregorianCalendar
        itemmonth = month!!.clone() as GregorianCalendar
        var adapter = CalendarAdapter(this, month, arrlist_calendar)

        homeBinding.routHome.setOnClickListener(this)
        homeBinding.routApply.setOnClickListener(this)
        homeBinding.routWfh.setOnClickListener(this)
        homeBinding.routOverTime.setOnClickListener(this)
        homeBinding.routSummary.setOnClickListener(this)
        homeBinding.headerLayout.txtManagerView.setOnClickListener(this)

        loginmodel = Login_Helper.getLogin(this@HomeActivity)
        if(loginmodel != null){
            if(!loginmodel.isManager) {
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                homeBinding.headerLayout.txtTitle.setPadding(150,0,0,0)
            }
            if(loginmodel.officeLocation.equals("UK"))
                homeBinding.routOverTime.visibility = View.GONE
        }
        key = intent.getStringExtra("key").toString()
        if(!key.isNullOrEmpty() && key.equals("summary")){
            homeBinding.headerLayout.txtTitle.text = "Leave Summary"
            homeBinding.headerLayout.txtManagerView.visibility = View.GONE
            homeBinding.headerLayout.txtTitle.setPadding(0,0,0,0)
            Utility.addFragment(LeaveSummaryFragment(), supportFragmentManager, R.id.frame_container)
            homeBinding.imgHome.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeBinding.imgSummary.setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeBinding.imgApply.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeBinding.imgWfh.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeBinding.imgOvertime.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

            homeBinding.viewHome.visibility = View.INVISIBLE
            homeBinding.viewSummary.visibility = View.VISIBLE
            homeBinding.viewApply.visibility = View.INVISIBLE
            homeBinding.viewShort.visibility = View.INVISIBLE
            homeBinding.viewOverTime.visibility = View.INVISIBLE
        }
        else {
            homeBinding.headerLayout.txtTitle.text = "Home"
            Utility.addFragment(HomeFragment(), supportFragmentManager, R.id.frame_container)
            homeBinding.imgHome.setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeBinding.imgSummary.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgApply.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgWfh.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgOvertime.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );

            homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

            homeBinding.viewHome.visibility = View.VISIBLE
             homeBinding.viewSummary.visibility = View.INVISIBLE
            homeBinding.viewApply.visibility = View.INVISIBLE
            homeBinding.viewShort.visibility = View.INVISIBLE
            homeBinding.viewOverTime.visibility = View.INVISIBLE

        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtManagerView -> {
                Utility.sendActionEvent("ManagerView_Button_Android",this)
                val intent = Intent(this, ManagerViewActivity::class.java)
                startActivity(intent)
            }R.id.routHome -> {
                Utility.sendActionEvent("Home_Button_Android",this)
                if(!loginmodel.isManager)
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                else
                homeBinding.headerLayout.txtManagerView.visibility = View.VISIBLE

                homeBinding.imgHome.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgSummary.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgApply.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgWfh.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgOvertime.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

                homeBinding.viewHome.visibility = View.VISIBLE
                 homeBinding.viewSummary.visibility = View.INVISIBLE
                homeBinding.viewApply.visibility = View.INVISIBLE
                homeBinding.viewShort.visibility = View.INVISIBLE
                homeBinding.viewOverTime.visibility = View.INVISIBLE

                homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

                homeBinding.headerLayout.txtTitle.text = "Home"
                Utility.replaceFragment(
                    HomeFragment(),
                    supportFragmentManager,
                    R.id.frame_container
                )
                if(!loginmodel.isManager) {
                    homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                    homeBinding.headerLayout.txtTitle.setPadding(150,0,0,0)
                }
            }
            R.id.routSummary -> {
                Utility.sendActionEvent("LeaveSummary_Button_Android",this)
                homeBinding.headerLayout.txtTitle.text = "Leave Summary"
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                homeBinding.headerLayout.txtTitle.setPadding(0,0,0,0)
                homeBinding.imgHome.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgSummary.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgApply.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgWfh.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgOvertime.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

                homeBinding.viewHome.visibility = View.INVISIBLE
                homeBinding.viewSummary.visibility = View.VISIBLE
                homeBinding.viewApply.visibility = View.INVISIBLE
                homeBinding.viewShort.visibility = View.INVISIBLE
                homeBinding.viewOverTime.visibility = View.INVISIBLE

                homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

                Utility.replaceFragment(LeaveSummaryFragment(), supportFragmentManager, R.id.frame_container)

            }
            R.id.routApply -> {
                Utility.sendActionEvent("ApplyLeave_Button_Android",this)
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE

                homeBinding.headerLayout.txtTitle.setPadding(0,0,0,0)
                homeBinding.imgHome.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgSummary.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgApply.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgWfh.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgOvertime.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

                homeBinding.viewHome.visibility = View.INVISIBLE
                 homeBinding.viewSummary.visibility = View.INVISIBLE
                homeBinding.viewApply.visibility = View.VISIBLE
                homeBinding.viewShort.visibility = View.INVISIBLE
                homeBinding.viewOverTime.visibility = View.INVISIBLE

                homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

                homeBinding.headerLayout.txtTitle.text = "Apply Request"
                Utility.replaceFragment(ApplyLeaveFragment(), supportFragmentManager, R.id.frame_container)
            }
            R.id.routWfh -> {
                Utility.sendActionEvent("ShortLeave_Button_Android",this)
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                homeBinding.headerLayout.txtTitle.text = "Short Leave"
                homeBinding.headerLayout.txtTitle.setPadding(50,0,0,0)

                homeBinding.imgHome.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgSummary.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgApply.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgWfh.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgOvertime.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

                homeBinding.viewHome.visibility = View.INVISIBLE
                 homeBinding.viewSummary.visibility = View.INVISIBLE
                homeBinding.viewApply.visibility = View.INVISIBLE
                homeBinding.viewShort.visibility = View.VISIBLE
                homeBinding.viewOverTime.visibility = View.INVISIBLE

                homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

                Utility.replaceFragment(ShortLeaveFragment(), supportFragmentManager, R.id.frame_container)
            }
            R.id.routOverTime -> {
                Utility.sendActionEvent("OvertimeLeave_Button_Android",this)
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                homeBinding.headerLayout.txtTitle.text = "Apply Overtime"
                homeBinding.headerLayout.txtTitle.setPadding(0,0,0,0)
                homeBinding.imgHome.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgSummary.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgApply.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgWfh.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                homeBinding.imgOvertime.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

                homeBinding.viewHome.visibility = View.INVISIBLE
                 homeBinding.viewSummary.visibility = View.INVISIBLE
                homeBinding.viewApply.visibility = View.INVISIBLE
                homeBinding.viewShort.visibility = View.INVISIBLE
                homeBinding.viewOverTime.visibility = View.VISIBLE

                homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
                homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                Utility.replaceFragment(ApplyOvertimeFragment(), supportFragmentManager, R.id.frame_container)
            }
        }
    }

    override fun onBackPressed() {

        if (homeBinding.headerLayout.txtTitle.text.equals("Home")) {
            finishAffinity()
        } else {
            homeBinding.headerLayout.txtTitle.text = "Home"
            Utility.replaceFragment(
                HomeFragment(),
                supportFragmentManager,
                R.id.frame_container
            )
            if (!loginmodel.isManager) {
                homeBinding.headerLayout.txtManagerView.visibility = View.GONE
                homeBinding.headerLayout.txtTitle.setPadding(150, 0, 0, 0)
            } else
                homeBinding.headerLayout.txtManagerView.visibility = View.VISIBLE

            homeBinding.imgHome.setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgSummary.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgApply.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgWfh.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );
            homeBinding.imgOvertime.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.SRC_IN
            );

            homeBinding.txtHome.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            homeBinding.txtSummary.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtApply.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtWfh.setTextColor(ContextCompat.getColor(this, R.color.black))
            homeBinding.txtOverTime.setTextColor(ContextCompat.getColor(this, R.color.black))

            homeBinding.viewHome.visibility = View.VISIBLE
             homeBinding.viewSummary.visibility = View.INVISIBLE
            homeBinding.viewApply.visibility = View.INVISIBLE
            homeBinding.viewShort.visibility = View.INVISIBLE
            homeBinding.viewOverTime.visibility = View.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        fragment!!.onActivityResult(requestCode, resultCode, data)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        fragment!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}