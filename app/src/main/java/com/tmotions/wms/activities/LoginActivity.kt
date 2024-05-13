package com.tmotions.wms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.tmotions.wms.R
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Utility
import com.tmotions.wms.common.Utility.displayAlertDialog
import com.tmotions.wms.databinding.ActivityLoginBinding
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.SentOtpResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityLoginBinding
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
          //  sendNotification(this)
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideActionBar(this, window)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.btnSendOtp.setOnClickListener(this)
      //  binding.loutEmail.setOnClickListener(this)
        binding.edtEmail.filters = arrayOf(Utility.ignoreFirstWhiteSpace())
        binding.edtEmail.filters = arrayOf(Utility.ignoreAllWhiteSpace())

        Utility.sendViewScreenEvent("Login_Activity_Android",this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
          //  checkNotificationPermission()
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    Log.e(TAG, "onCreate: PERMISSION GRANTED")
                  //  sendNotification(this)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Snackbar.make(
                        findViewById(R.id.parent_layout),
                        "WMS needs permission to send and receive push notification",
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        // Responds to click on the action
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }.show()
                }
                else -> {
                    // The registered ActivityResultCallback gets the result of this request
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
     }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSendOtp -> {
                if (binding.edtEmail.text.toString().trim().isEmpty()) {
                    displayAlertDialog(this,resources.getString(R.string.please_provide_a_valid_email_address),layoutInflater)
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString()).matches()) {
                    displayAlertDialog(this,resources.getString(R.string.please_provide_a_valid_email_address),layoutInflater)
                }
                else{
                    if(Utility.isOnline(this)) {
                        Utility.sendActionEvent("Login_Button_Android",this)
                        getUserLogin()
                    }
                    else
                        displayAlertDialog(this@LoginActivity,resources.getString(R.string.nointernet),layoutInflater)
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getUserLogin() {
        Utility.setWindowNotClickable(window)
//        binding.btnSendOtp.isEnabled = false
        binding.edtEmail.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        var loginModel = LoginModel()
        loginModel.emailId =  binding.edtEmail.text.toString()

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<SentOtpResponseModel> = retrofitApiInterface.sendOTPToEmail("",loginModel)

        mService.enqueue(object : Callback<SentOtpResponseModel> {
            override fun onResponse(call: Call<SentOtpResponseModel>, response: Response<SentOtpResponseModel>) {
                Utility.setWindowClickable(window)

//                binding.btnSendOtp.isEnabled = true
                binding.edtEmail.isEnabled = true
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            Toast.makeText(this@LoginActivity,baseResponseModel.message, Toast.LENGTH_LONG).show()
                            val intent = Intent(this@LoginActivity, ActivityVerifyOTP::class.java)
                            intent.putExtra("emailId",binding.edtEmail.text.toString())
                            startActivity(intent)
                            finish()
                        }  else {
                            displayAlertDialog(this@LoginActivity,baseResponseModel.message,layoutInflater)
                        }
                    }
                    else {
                        displayAlertDialog(this@LoginActivity,"${response.message()}",layoutInflater)
                     }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<SentOtpResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                displayAlertDialog(this@LoginActivity,"$t",layoutInflater)
                binding.edtEmail.isEnabled = true
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun checkNotificationPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.POST_NOTIFICATIONS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                    if (response.isPermanentlyDenied) {
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) { /* ... */
                }
            }).check()
    }

    private fun showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        val builder = AlertDialog.Builder(this@LoginActivity)

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions")
        builder.setCancelable(false)

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to send push notification. You can grant them in app settings.")
        builder.setPositiveButton("Go to Settings") { dialog, which ->
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel()
            // below is the intent from which we are redirecting our user.
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // this method is called when user click on negative button.
            dialog.cancel()
        }
        // below line is used to display our dialog
        builder.show()
    }

    companion object {
        const val TAG = "MainActivity"
        const val NOTIFICATION_MESSAGE_TAG = "message from notification"
    }

}