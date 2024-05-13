package com.tmotions.wms.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.tmotions.wms.R
import com.tmotions.wms.activities.HomeActivity
import java.io.*
import java.lang.reflect.Method
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


object Utility {
    val MY_PREFS_NAME = "wms_pref"

    fun addFragment(fragment: Fragment?, fragmentManager: FragmentManager, resId: Int) {
        try {
            if (fragment != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.add(resId, fragment).commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun replaceFragment(fragment: Fragment?, fragmentManager: FragmentManager, resId: Int) {
        if (fragment != null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            //   fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(resId, fragment)
            // fragmentTransaction.commit();
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

     fun saveStringPreference(key: String?, value: String?) {
        val editor =
            MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun saveIntPreference(key: String?, value: Int) {
        val editor =
            MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    fun saveBooleanPreference(key: String?, value: Boolean) {
        val editor =
            MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getIntPreference(key: String?): Int? {
        return MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            ?.getInt(key, 0)
    }

    fun getStringPreference(key: String?): String? {
        return MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(key, null)
    }

    fun getBooeanPreference(key: String?): Boolean? {
        return MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            ?.getBoolean(key, false)
    }

    fun resetSearchPreference() {
        try {
            saveStringPreference("checkcomingstatus", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetPreference() {
        try {
            saveStringPreference("checkcomingstatus", "")
            saveStringPreference("order_total", "")
            saveIntPreference("orderId", 0)
            saveStringPreference("isShowing", "")
            saveStringPreference("PaymentMethodSystemName", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun storeString(key: String, text: String) {
        val editor =
            MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)!!
                .edit()
        editor.putString(key, text)
        editor.commit()
    }

    fun getString(key: String, def: String): String {
        val text = MyApplication.instance?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(key, def) ?: ""
        return text
    }


    fun snackBar(view: View?, msg: String?) {
        val sb = Snackbar.make(view!!, msg!!, 800)
        val sbView = sb.view
        sbView.setBackgroundColor(getColorHelper(MyApplication.instance!!, R.color.red))
        sb.show()
    }

    fun getColorHelper(context: Context, @ColorRes id: Int) =
        if (Build.VERSION.SDK_INT >= 23) context.getColor(id) else context.resources.getColor(id);

    fun showProgressDialog(context: Context): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.LEFT
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.LEFT
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = "Fetching current location..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 17.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    fun validateEmail(context: Context, editText: EditText, email: TextInputLayout): Boolean {
        if (editText.getText().toString().trim().isEmpty()) {
            email.setError("Email address required")
            return false
            // email.setErrorEnabled(false)
        } else {
            val emailId: String = editText.getText().toString()
            val isValid = Patterns.EMAIL_ADDRESS.matcher(emailId).matches()
            if (!isValid) {
                email.setError("Invalid Email address, ex: abc@example.com")
                // requestFocus(context,editText)
                return false
            } else {
                email.setErrorEnabled(false)
            }
        }
        return true
    }

//    private fun requestFocus(context: Context, view: View) {
//        if (view.requestFocus()) {
//            MyApplication.instance.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//        }
//    }




    fun isBlank(editText: EditText, textInputLayout: TextInputLayout, errorMsg: String): Boolean {
        if (editText.getText().toString().trim().isEmpty()) {
            textInputLayout.setError(errorMsg)
            // requestFocus(password)
            return false
        } else {
            textInputLayout.setErrorEnabled(false)
        }
        return true
    }

    fun hideKeyboard(context: Context, view: View) {
        view?.apply {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun displayMessages(context: Context, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
        }
        builder.show()
    }

    fun hideActionBar(context: Context, window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = true
            }
        else
            window.statusBarColor =
                ContextCompat.getColor(context,  R.color.black)
    }

    fun getBitmap(path: String?): Bitmap? {
        var bitmp: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmp = BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmp
    }

    fun setPic(imagePath: String?, maxHeight: Int, maxWidth: Int) {
        // Get the dimensions of the bitmap
        try {
            var photoBm = getBitmap(imagePath)
            //get its orginal dimensions
            val bmOriginalWidth = photoBm!!.width
            val bmOriginalHeight = photoBm.height
            val originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight
            val originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth
            //choose a maximum height

            //call the method to get the scaled bitmap
            photoBm = getScaledBitmap(
                photoBm, bmOriginalWidth, bmOriginalHeight,
                originalWidthToHeightRatio, originalHeightToWidthRatio,
                maxHeight, maxWidth
            )
            /**********THE REST OF THIS IS FROM Prabu's answer */
            //create a byte array output stream to hold the photo's bytes
            val bytes = ByteArrayOutputStream()
            //compress the photo's bytes into the byte array output stream
            photoBm!!.compress(Bitmap.CompressFormat.PNG, 80, bytes)
            val fo = FileOutputStream(imagePath)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (se: Exception) {
        }
    }

    fun getScaledBitmap(
        bm: Bitmap?,
        bmOriginalWidth: Int,
        bmOriginalHeight: Int,
        originalWidthToHeightRatio: Double,
        originalHeightToWidthRatio: Double,
        maxHeight: Int,
        maxWidth: Int
    ): Bitmap? {
        var bm = bm
        if (bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {
            Log.v(
                ContentValues.TAG,
                String.format("RESIZING bitmap FROM %sx%s ", bmOriginalWidth, bmOriginalHeight)
            )
            bm = if (bmOriginalWidth > bmOriginalHeight) {
                scaleDeminsFromWidth(
                    bm,
                    maxWidth,
                    bmOriginalHeight,
                    originalHeightToWidthRatio
                )
            } else {
                scaleDeminsFromHeight(
                    bm,
                    maxHeight,
                    bmOriginalHeight,
                    originalWidthToHeightRatio
                )
            }
            Log.v(
                ContentValues.TAG,
                String.format("RESIZED bitmap TO %sx%s ", bm!!.width, bm.height)
            )
        }
        return bm
    }

    private fun scaleDeminsFromHeight(
        bm: Bitmap?,
        maxHeight: Int,
        bmOriginalHeight: Int,
        originalWidthToHeightRatio: Double
    ): Bitmap? {
        var bm = bm
        val newHeight = Math.min(maxHeight, bmOriginalHeight)
        val newWidth = (newHeight * originalWidthToHeightRatio).toInt()
        bm = Bitmap.createScaledBitmap(bm!!, newWidth, newHeight, true)
        return bm
    }

    private fun scaleDeminsFromWidth(
        bm: Bitmap?,
        maxWidth: Int,
        bmOriginalWidth: Int,
        originalHeightToWidthRatio: Double
    ): Bitmap? {
        //scale the width
        var bm = bm
        val newWidth = Math.min(maxWidth, bmOriginalWidth)
        val newHeight = (newWidth * originalHeightToWidthRatio).toInt()
        bm = Bitmap.createScaledBitmap(bm!!, newWidth, newHeight, true)
        return bm
    }

    fun checkisNullOrEmpty(var0: String?): Boolean {
        return var0 == null || var0.trim { it <= ' ' }.isEmpty() || var0.trim { it <= ' ' }.equals(
            "null",
            ignoreCase = true
        )
    }

    fun isOnline(context: Context): Boolean {
        var connection = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            connection = netInfo != null && netInfo.isConnectedOrConnecting
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return connection
    }

    fun setTextInputLayoutHintColor(
        textInputLayout: TextInputLayout,
        context: Context,
        @ColorRes colorIdRes: Int
    ) {
        textInputLayout.defaultHintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorIdRes))
        textInputLayout.boxStrokeErrorColor =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorIdRes))
    }

    fun isEmpty(editText: EditText): Boolean {
        var isEmptyResult = false
        if (editText.text.length == 0) {
            isEmptyResult = true
        }
        return isEmptyResult
    }

    fun myDateFormat(dayOfMonth: Int, monthOfYear: Int, year: Int): String? {
        var monthOfYear = monthOfYear
        monthOfYear += 1
        val mt: String
        val dy: String //local variable
        mt = if (monthOfYear < 10) "0$monthOfYear" //if month less than 10 then ad 0 before month
        else monthOfYear.toString()
        dy = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
         return "$mt/$dy/$year"
        //return "$year-$mt-$dy"
    }

    fun sendDateFormat(dayOfMonth: Int, monthOfYear: Int, year: Int): String {
        var monthOfYear = monthOfYear
        monthOfYear += 1
        val mt: String
        val dy: String //local variable
        mt = if (monthOfYear < 10) "0$monthOfYear" //if month less than 10 then ad 0 before month
        else monthOfYear.toString()
        dy = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
        return "$year-$mt-$dy"
    }

    @SuppressLint("SuspiciousIndentation")
    fun displayDateFormat(dayOfMonth: Int, monthOfYear: Int, year: Int): String {
        var monthOfYear = monthOfYear
        monthOfYear += 1
        val mt: String
        val dy: String //local variable
        mt = if (monthOfYear < 10) "0$monthOfYear" //if month less than 10 then ad 0 before month
        else monthOfYear.toString()
        dy = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
            return "$dy/$mt/$year"
    }

    fun setUpdateDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        //  var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("MM/dd/yyyy")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun setMyProfileDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("MM-dd-yyyy")
        //  var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("MM/dd/yyyy")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun setDisplayDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("yyyy-MM-dd")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun getTwoDecimalPlacesValue(doubleValue: Double): String {
        return String.format(Locale.US, "%.2f", doubleValue)
    }

    fun getTwoDecimalPlacesValue2(doubleValue: String): String {
        return String.format(Locale.US, "%.2f", doubleValue)
    }

    fun displayOrderDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            val varDate = dateFormat.parse(strDate)
            dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm a")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun displayReviewDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a")
        try {
            val varDate = dateFormat.parse(strDate)
            dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm a")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }


    fun getDateFromUtc(ourDate: String): String? {
        var ourDate: String? = ourDate
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm a")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm a") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)
            //Log.d("ourDate", ourDate);
        } catch (e: java.lang.Exception) {
            ourDate = "00-00-0000 00:00"
        }
        return ourDate
    }

    fun getCurrentDate(): String? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = Date()
        return dateFormat.format(date)
    }

    fun getCurrentDateSend(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return dateFormat.format(date)
    }


    fun getBooleanValue(value: String?): Boolean {
        var i = false
        try {
            i = java.lang.Boolean.parseBoolean(value)
        } catch (e: java.lang.Exception) {
        }
        return i
    }

    fun getMyDrawable(context: Context, id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getMonthNumber(monthName: String): String { // month name starts from 1 to 12
        var monthname = "January"
        when (monthName) {
            "January" -> monthname = "1"
            "February" -> monthname = "2"
            "March" -> monthname = "3"
            "April" -> monthname = "4"
            "May" -> monthname = "5"
            "June" -> monthname = "6"
            "July" -> monthname = "7"
            "August" -> monthname = "8"
            "September" -> monthname = "9"
            "October" -> monthname = "10"
            "November" -> monthname = "11"
            "December" -> monthname = "12"

        }
        return monthname
    }

    fun getMonth(date: Date?): Int {
        val calender = Calendar.getInstance()
        calender.time = date
        return calender[Calendar.MONTH]+1
    }

    fun getDay(date: Date?): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal[Calendar.DAY_OF_MONTH]
    }
    fun getYear(date: Date?): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal[Calendar.YEAR]
    }

    fun getScreenWidth(context: Context): Int {
        var returnWidth : Int
        try {

            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            returnWidth=width
            return returnWidth
        } catch (error: Exception) {
            //  Log.d(AppData.TAG, "Error : autoCreateTable()", error)
        }

        return 0
    }

    fun setPopupWindowTouchModal(popupWindow: PopupWindow?, touchModal: Boolean) {
        if (null == popupWindow) {
            return
        }
        val method: Method
        try {
            method = PopupWindow::class.java.getDeclaredMethod(
                "setTouchModal",
                Boolean::class.javaPrimitiveType
            )
            method.setAccessible(true)
            method.invoke(popupWindow, touchModal)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun setActionBar(context: Context,window: Window){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = true
            }
        else
            window.statusBarColor = ContextCompat.getColor(context, R.color.black)
    }

    fun setCalanderDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("dd MMMM yyyy")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("dd MMM yyyy")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun setCalanderDateAfterDateSelection(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("dd/MM/yyyy")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun testDate(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("yyyy-MM-dd")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")
            dateFormat = SimpleDateFormat("MM-dd-yyyy")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun testDate2(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("MM-dd-yyyy")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")yyyy-MM-dd
            dateFormat = SimpleDateFormat("yyyy-MM-dd")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun testDate3(strDate: String?): String? {
        var date1: String? = null
        var dateFormat = SimpleDateFormat("MM-dd-yyyy")
        try {
            val varDate = dateFormat.parse(strDate)
            //  dateFormat = SimpleDateFormat("dd-MM-yyyy")yyyy-MM-dd
            dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            date1 = dateFormat.format(varDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date1
    }

    fun hideKeyboard(activity: AppCompatActivity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showSoftKeyboard(context: Context, editText: EditText) {
        try {
            editText.requestFocus()
            editText.postDelayed(
                {
                    val keyboard =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyboard.showSoftInput(editText, 0)
                }, 200)
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateByUserTimeZone(date: String): String {
        val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a")
        val dateex = Instant.parse(date)
        val dateTime = dateex.atZone(ZoneId.systemDefault())
        val formattedDate: String = dateTime.format(FORMATTER)
        return formattedDate

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateInLocalTimeZone(date: String): String {
        val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a")
        val localDate = LocalDate.parse(date, FORMATTER)
        return localDate.toString()

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateTimeByUserTimeZone(date: String): String {
        val FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a")
        val dateex = Instant.parse(date)
        val dateTime = dateex.atZone(ZoneId.systemDefault())
        val formattedDate: String = dateTime.format(FORMATTER)
        return formattedDate
    }

    fun getDateCompare(fistDate : String, secondDate : String): Boolean {
        var isCompare = false

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val convertedDate = dateFormat.parse(fistDate)
            val convertedDate2 = dateFormat.parse(secondDate)
            if (convertedDate2.equals(convertedDate)) {
                isCompare= true
            } else {
                isCompare= false
            }
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return isCompare
    }

    fun isNullOrEmpty(var0: String?): Boolean {
        return var0 == null || var0.trim { it <= ' ' }.isEmpty() || var0.trim { it <= ' ' }
            .equals("null", ignoreCase = true)
    }

     fun displayAlertDialog(context: Context,message: String,layoutInflater: LayoutInflater) {
        val alertDialog = AlertDialog.Builder(context)
        val alert: AlertDialog = alertDialog.create()
        val convertView = layoutInflater.inflate(R.layout.alert_dialog_layout, null) as View
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.setCancelable(false)
        alert.setView(convertView)
        val txtmsg = convertView.findViewById<TextView>(R.id.txtmsg)
        val btnDismiss = convertView.findViewById<Button>(R.id.dialogDismiss_button)

        txtmsg.text = message

        btnDismiss.setOnClickListener {
            alert.cancel()
        }

        alert.show()
    }

    public fun displaySuccessDialog(context: Context,message: String,layoutInflater: LayoutInflater) {
        val alertDialog = AlertDialog.Builder(context)
        val alert: AlertDialog = alertDialog.create()
        val convertView = layoutInflater.inflate(R.layout.alert_dialog_layout, null) as View
        alert.setCancelable(false)
        alert.setView(convertView)
        val txtmsg = convertView.findViewById<TextView>(R.id.txtmsg)
        val btnDismiss = convertView.findViewById<Button>(R.id.dialogDismiss_button)

        txtmsg.text = message

        btnDismiss.setOnClickListener {
            alert.cancel()
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
            (context as Activity).finish()

        }

        alert.show()
    }

    fun getSendDate(ourDate: String): String {
        var ourDate: String = ourDate
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)
        } catch (e: java.lang.Exception) {
            ourDate = "00-00-0000"
        }
        return ourDate
    }

    fun sendRHDate(ourDate: String): String {
        var ourDate: String = ourDate
        try {
            val formatter = SimpleDateFormat("dd MMM yyyy")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)
        } catch (e: java.lang.Exception) {
            ourDate = "00-00-0000"
        }
        return ourDate
    }

    fun getColoredSpanned(text: String, color: String): String? {
        return "<font color=$color>$text</font>"
    }

    fun getcurrentDateAndTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentDateandTime = sdf.format(Date())
        return  currentDateandTime
    }

    fun getcurrentTime(): String {
        val sdf = SimpleDateFormat("'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val utcTime: String = sdf.format(Date())
        return utcTime
    }

    fun getSendDate2(ourDate: String): String {
        var ourDate: String = ourDate
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourDate)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") //this format changeable
            dateFormatter.timeZone = TimeZone.getDefault()
            ourDate = dateFormatter.format(value)
        } catch (e: java.lang.Exception) {
            ourDate = "00-00-0000"
        }
        return ourDate
    }

     fun shouldShowRequestPermissionRationale(
        activity: Activity?,
        vararg permissions: String?
    ): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission!!)) {
                return true
            }
        }
        return false
    }

    fun handleMultilineEditText(edtReason: EditText){
        edtReason.isVerticalScrollBarEnabled = true
        edtReason.overScrollMode = View.OVER_SCROLL_ALWAYS
        edtReason.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        edtReason.movementMethod = ScrollingMovementMethod.getInstance()
        // binding.edtReason.setImeOptions(EditorInfo.IME_ACTION_DONE || EditorInfo.IME_ACTION_NEXT)
        edtReason.setRawInputType(InputType.TYPE_CLASS_TEXT)

//        edtReason.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
//            view.parent.requestDisallowInterceptTouchEvent(true)
//            if (motionEvent.action and MotionEvent.ACTION_UP != 0 && motionEvent.actionMasked and MotionEvent.ACTION_UP != 0) {
//                view.parent.requestDisallowInterceptTouchEvent(false)
//            }
//            false
//        })
    }

//    fun ignoreFirstWhiteSpace(): InputFilter? {
//        return InputFilter { source, start, end, dest, dstart, dend ->
//            for (i in start until end) {
//                if (Character.isWhitespace(source[i])) {
//                    if (dstart == 0) return@InputFilter ""
//                }
//            }
//            null
//        }
//    }

    fun ignoreFirstWhiteSpace(): InputFilter? {
        return InputFilter { source, start, end, dest, dstart, dend ->
            if (dstart == 0 && start < end && Character.isWhitespace(source[start])) {
                return@InputFilter ""
            }
            null
        }
    }

    fun ignoreAllWhiteSpace(): InputFilter? {
        return InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                   return@InputFilter ""
                }
            }
            null
        }
    }

    fun setWindowNotClickable(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun setWindowClickable(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

/*
    fun getAddressFromCoordinates(lat: Double, lng: Double):String {
        var countryName = ""
        try {
            val geocoder = Geocoder(MyApplication.instance, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addressForAPI33AndAbove =
                    geocoder.getFromLocation(lat, lng, 1) { p0 ->
                        val address = p0[0]
                        if (address != null)
                            countryName = address.countryName
                    }
            } else {
                val address = geocoder.getFromLocation(lat, lng, 1)?.get(0)
                if (address != null)
                    countryName = address.countryName
            }
        } catch (e: IOException) {
            // Utils.log("Check internet: ${e.message}")
        }
        return countryName
    }*/


    fun sendActionEvent(buttonName: String, context: Context){
       var firebaseAnalytics = FirebaseAnalytics.getInstance(context)
       firebaseAnalytics.logEvent(buttonName, null)
    }

    fun sendViewScreenEvent(activityName: String,context: Context){
        var firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activityName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, activityName)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun showLocationSnackbar(context: Context,layoutInflater: LayoutInflater,view : View):Snackbar {

        // Create the Snackbar
        val objLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
      // var snackbar = Snackbar.make(findViewById<View>(android.R.id.content), " ", Snackbar.LENGTH_INDEFINITE)
       var snackbar = Snackbar.make(view, " ", Snackbar.LENGTH_INDEFINITE)

        // Get the Snackbar layout view
        val layout = snackbar!!.getView() as Snackbar.SnackbarLayout

        // Set snackbar layout params
        val navbarHeight: Int = getNavBarHeight(context)
        val parentParams = layout.layoutParams as FrameLayout.LayoutParams
        parentParams.setMargins(0, 0, 0, 0 - navbarHeight + 50)
        layout.layoutParams = parentParams
        layout.setPadding(0, 0, 0, 0)
        layout.layoutParams = parentParams

        // Inflate our custom view
        val snackView: View = layoutInflater.inflate(R.layout.location_snackbar_layout, null)

        // Configure our custom view
        val messageTextView = snackView.findViewById<View>(R.id.message_text_view) as TextView
        messageTextView.setText("Fetching your location, please wait.")

        // Add our custom view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams)
        // Show the Snackbar
        snackbar!!.show()
        return snackbar
    }

    fun getNavBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun playBeep(context: Context){
        var mediaPlayer = MediaPlayer.create(context, R.raw.beep)
        mediaPlayer.start()
    }
}