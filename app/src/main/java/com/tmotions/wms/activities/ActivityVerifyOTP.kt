package com.tmotions.wms.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.tmotions.wms.R
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityVerifyOtpBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.RequestOtpVerifyModel
import com.tmotions.wms.models.SentOtpResponseModel
import com.tmotions.wms.models.VerifyOTPResponseModel
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit


class ActivityVerifyOTP : AppCompatActivity(), View.OnClickListener, TextWatcher {

    lateinit var binding: ActivityVerifyOtpBinding
    private lateinit var editTexts: Array<EditText?>
    private var emailId = ""
    private var firebaseToken = ""
    private var firebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideActionBar(this, window)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_otp)

        FirebaseApp.initializeApp(this)
        emailId = intent.getStringExtra("emailId").toString()

        editTexts = arrayOf(binding.edtOtp1, binding.edtOtp2, binding.edtOtp3, binding.edtOtp4)

        Utility.sendViewScreenEvent("VerifyOtp_Activity_Android",this)

        binding.edtOtp1.addTextChangedListener(this)
        binding.edtOtp2.addTextChangedListener(this)
        binding.edtOtp3.addTextChangedListener(this)
        binding.edtOtp4.addTextChangedListener(this)


        binding.edtOtp1.addTextChangedListener(PinTextWatcher(0))
        binding.edtOtp2.addTextChangedListener(PinTextWatcher(1))
        binding.edtOtp3.addTextChangedListener(PinTextWatcher(2))
        binding.edtOtp4.addTextChangedListener(PinTextWatcher(3))

        binding.edtOtp1.setOnKeyListener(PinOnKeyListener(0))
        binding.edtOtp2.setOnKeyListener(PinOnKeyListener(1))
        binding.edtOtp3.setOnKeyListener(PinOnKeyListener(2))
        binding.edtOtp4.setOnKeyListener(PinOnKeyListener(3))

        binding.btnVerifyOtp.setOnClickListener(this)
        binding.txtResendOtp.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        countDown()

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            firebaseToken = it.result.toString()
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "my screen classs")
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "my custom screen name")
        firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVerifyOtp -> {
                if (binding.edtOtp1.text.isNullOrEmpty() || binding.edtOtp2.text.isNullOrEmpty() || binding.edtOtp3.text.isNullOrEmpty() || binding.edtOtp1.text.isNullOrEmpty()) {
                    Utility.displayAlertDialog(this,resources.getString(R.string.kindly_input_the_otp),layoutInflater)
                } else {
                    val userEnteredOtp: String = binding.edtOtp1.text.toString() +
                            binding.edtOtp2.text.toString() +
                            binding.edtOtp3.text.toString() +
                            binding.edtOtp4.text.toString()
                    if (Utility.isOnline(this)) {
                        Utility.sendActionEvent("VerifyOtp_Button_Android",this)
                        verifyOTP(userEnteredOtp)
                    } else {
                       Utility.displayAlertDialog(this,resources.getString(R.string.nointernet),layoutInflater)
                    }
                }
            }
            R.id.txtResendOtp ->{
                if (Utility.isOnline(this)) {
                    Utility.sendActionEvent("ResendOtp_Button_Android",this)
                    countDown()
                    getResendOtp()
                }
                else
                Utility.displayAlertDialog(
                    this@ActivityVerifyOTP,
                    resources.getString(R.string.nointernet),
                    layoutInflater
                )

            }
            R.id.imgBack ->{
                val intent = Intent(this@ActivityVerifyOTP, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if(binding.edtOtp1.text.toString().isNotEmpty() && binding.edtOtp2.text.toString().isNotEmpty()
            && binding.edtOtp3.text.toString().isNotEmpty() && binding.edtOtp4.text.toString().isNotEmpty()){
            Utility.hideKeyboard(this)
        }else {
            if (binding.edtOtp1.isFocused && binding.edtOtp1.text.toString().trim().length == 1)
                binding.edtOtp2.requestFocus(View.FOCUS_RIGHT)
            else if (binding.edtOtp2.isFocused && binding.edtOtp2.text.toString()
                    .trim().length == 1
            )
                binding.edtOtp3.requestFocus(View.FOCUS_RIGHT)
            else if (binding.edtOtp3.isFocused && binding.edtOtp3.text.toString()
                    .trim().length == 1
            )
                binding.edtOtp4.requestFocus(View.FOCUS_RIGHT)
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    inner class PinTextWatcher internal constructor(private val currentIndex: Int) : TextWatcher {
        private var isFirst = false
        private var isLast = false
        private var newTypedString = ""

        init {
            if (currentIndex == 0) isFirst =
                true else if (currentIndex == editTexts.size - 1) isLast = true
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            newTypedString = s.subSequence(start, start + count).toString().trim { it <= ' ' }
        }

        override fun afterTextChanged(s: Editable) {
            var text = newTypedString
            // Detect paste event and set first char
            if (text.length > 1) text = text[0].toString()
            editTexts[currentIndex]!!.removeTextChangedListener(this)
            editTexts[currentIndex]!!.setText(text)
            editTexts[currentIndex]!!.setSelection(text.length)
            editTexts[currentIndex]!!.addTextChangedListener(this)
            if (text.length == 1) moveToNext() else if (text.isEmpty()) moveToPrevious()
        }

        private fun moveToNext() {
            if (!isLast) editTexts[currentIndex + 1]!!.requestFocus()
            if (isAllEditTextsFilled && isLast) { // isLast is optional
                editTexts[currentIndex]!!.clearFocus()
                hideKeyboard()
            }
        }

        private fun moveToPrevious() {
            if (!isFirst) editTexts[currentIndex - 1]!!.requestFocus()
        }

        private val isAllEditTextsFilled: Boolean
            get() {
                for (editText in editTexts) if (editText!!.text.toString()
                        .trim { it <= ' ' }.isEmpty()
                ) return false
                return true
            }

        private fun hideKeyboard() {
            if (currentFocus != null) {
                val inputMethodManager =
                     getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                     currentFocus!!.windowToken,
                    0
                )
            }
        }
    }

    inner class PinOnKeyListener internal constructor(private val currentIndex: Int) :
        View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (editTexts[currentIndex]!!
                        .text.toString().isEmpty() && currentIndex != 0
                ) editTexts[currentIndex - 1]!!.requestFocus()
            }
            return false
        }
    }

    private fun verifyOTP(userEnteredOtp: String) {
        Utility.setWindowNotClickable(window)
     //   binding.btnVerifyOtp.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        val requestOtpVerifyModel = RequestOtpVerifyModel()
        requestOtpVerifyModel.emailId =  emailId
        requestOtpVerifyModel.otp = userEnteredOtp
        requestOtpVerifyModel.firebaseDeviceToken = firebaseToken
        requestOtpVerifyModel.isAndroiodDevice = true

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<VerifyOTPResponseModel> = retrofitApiInterface.verifyOTP("",requestOtpVerifyModel)

        mService.enqueue(object : Callback<VerifyOTPResponseModel> {
            override fun onResponse(call: Call<VerifyOTPResponseModel>, response: Response<VerifyOTPResponseModel>) {
                Utility.setWindowClickable(window)
             //   binding.btnVerifyOtp.isEnabled = true
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                       if (baseResponseModel!!.statusCode == 200) {
                            val loginModel = baseResponseModel.data
                            if(loginModel != null){
                                if(Login_Helper.saveUserData(loginModel,this@ActivityVerifyOTP)) {
                                    val intent = Intent(
                                        this@ActivityVerifyOTP,
                                        HomeActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            else{
                                Utility.displayAlertDialog(
                                    this@ActivityVerifyOTP,
                                    baseResponseModel.message,
                                    layoutInflater
                                )
                            }
                        }
                        else {
                           Utility.displayAlertDialog(
                               this@ActivityVerifyOTP,
                               baseResponseModel.message,
                               layoutInflater
                           )
                        }
                    }
                    else {
                        Utility.displayAlertDialog(
                            this@ActivityVerifyOTP,
                            response.message(),
                            layoutInflater
                        )
                     }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<VerifyOTPResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ActivityVerifyOTP, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }

    private fun countDown() {
        object : CountDownTimer(120000, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                binding.txtResendOtp.isEnabled = false
                binding.txtResendOtp.setTextColor(ContextCompat.getColor(this@ActivityVerifyOTP, R.color.gray)
                )
                binding.timerOTPtxt.text = Html.fromHtml(
                    "<b>" + java.lang.String.format(
                        "%d min : %d sec" + "</b>",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                )
            }

            override fun onFinish() {
                binding.edtOtp1.setText("")
                binding.edtOtp2.setText("")
                binding.edtOtp3.setText("")
                binding.edtOtp4.setText("")
                binding.txtResendOtp.isEnabled = true
                binding.txtResendOtp.setTextColor(ContextCompat.getColor(this@ActivityVerifyOTP, R.color.color_E36D02)
                )
//                binding.edtOtp1.requestFocus(View.FOCUS_RIGHT)
            }
        }.start()
    }

    private fun getResendOtp() {
        Utility.setWindowNotClickable(window)
       // binding.txtResendOtp.isEnabled = false
        binding.mprogress.visibility = View.VISIBLE
        val loginModel = LoginModel()
        loginModel.emailId = emailId

        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<SentOtpResponseModel> = retrofitApiInterface.sendOTPToEmail("",loginModel)

        mService.enqueue(object : Callback<SentOtpResponseModel> {
            override fun onResponse(call: Call<SentOtpResponseModel>, response: Response<SentOtpResponseModel>) {
                Utility.setWindowClickable(window)
              //  binding.txtResendOtp.isEnabled = true
                binding.mprogress.visibility = View.GONE
                try {
                    if (response.isSuccessful) {
                        val baseResponseModel = response.body()
                        if (baseResponseModel!!.statusCode == 200) {
                            Toasty.success(this@ActivityVerifyOTP,baseResponseModel.message).show()
                        }
                        else {
                            Utility.displayAlertDialog(
                                this@ActivityVerifyOTP,
                                baseResponseModel.message,
                                layoutInflater
                            )
                        }
                    }
                    else {
                        Utility.displayAlertDialog(
                            this@ActivityVerifyOTP,
                            response.message(),
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<SentOtpResponseModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ActivityVerifyOTP, "$t", layoutInflater)
                binding.mprogress.visibility = View.GONE
            }
        })
    }
}