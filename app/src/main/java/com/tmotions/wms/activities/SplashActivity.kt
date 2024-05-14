package com.tmotions.wms.activities

import android.R.id
import android.R.id.message
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.tmotions.wms.R
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivitySplashBinding
import com.tmotions.wms.dbhelper.Login_Helper

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    var appUpdateManager: AppUpdateManager? = null
    val int = 0
    val demo = "hi"
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)
        Utility.hideActionBar(this, window)

        Utility.sendViewScreenEvent("Splash_Activity_Android",this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
    //    checkUpdate()
        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            proceedWithAppFlow()
        },3000)
        print("Hello world")
        print("Hello Tmotions")
        print("Hello world")
        print("abc")
        print("hii sir")
        print("new change")

        print("new pull")

        print("development")
    }
    private fun checkUpdate() {
        var appUpdateInfoTask = appUpdateManager!!.appUpdateInfo
        appUpdateInfoTask!!.addOnSuccessListener { appUpdateInfo ->
            if ((appUpdateInfo!!.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo!!.isUpdateTypeAllowed(IMMEDIATE))) {
                try {
                    showTwoButtonSnackbar()
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                Handler(Looper.myLooper()!!).postDelayed(Runnable {
                    proceedWithAppFlow()
                },3000)
             }
        }
    }

    private fun proceedWithAppFlow() {
        val loginModel = Login_Helper.getLogin(this@SplashActivity)
        if (loginModel != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)//LoginActivity
            intent.putExtra("key", "splash")
            startActivity(intent)
            finish()
        }
    }

    private fun installAppUpdateFromPlayStore() {
        val intentInstall = Intent(Intent.ACTION_VIEW)
        intentInstall.data = Uri.parse("market://details?id=$packageName")
        startActivity(intentInstall)
    }

    private fun showTwoButtonSnackbar() {

        // Create the Snackbar
        val objLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
       var snackbar =
            Snackbar.make(findViewById<View>(id.content), "Update available. Install now?", Snackbar.LENGTH_INDEFINITE)

        // Get the Snackbar layout view
        val layout = snackbar.getView() as SnackbarLayout

        // Set snackbar layout params
        val navbarHeight: Int = getNavBarHeight(this)
        val parentParams = layout.layoutParams as FrameLayout.LayoutParams
        parentParams.setMargins(0, 0, 0, 0 - navbarHeight + 50)
        layout.layoutParams = parentParams
        layout.setPadding(0, 0, 0, 0)
        layout.layoutParams = parentParams

        // Inflate our custom view
        val snackView: View = layoutInflater.inflate(R.layout.app_update_layout, null)

        // Configure our custom view
        val messageTextView = snackView.findViewById<View>(R.id.message_text_view) as TextView
        messageTextView.setText("Update available. Install now?")
        val textViewOne = snackView.findViewById<View>(R.id.first_text_view) as TextView
        textViewOne.text = "Cancel"
        textViewOne.setOnClickListener {
            snackbar.dismiss()
            proceedWithAppFlow()
        }
        val textViewTwo = snackView.findViewById<View>(R.id.second_text_view) as TextView
        textViewTwo.text = "Install"
        textViewTwo.setOnClickListener {
            snackbar.dismiss()
            installAppUpdateFromPlayStore()
        }
        // Add our custom view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams)
        // Show the Snackbar
        snackbar.show()
    }

    private fun getNavBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    override fun onResume() {
        super.onResume()
        checkUpdate()
    }
}