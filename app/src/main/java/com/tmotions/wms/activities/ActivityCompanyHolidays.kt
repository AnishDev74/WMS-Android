package com.tmotions.wms.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.tmotions.wms.R
import com.tmotions.wms.adapters.CalendarAdapter
import com.tmotions.wms.adapters.CompanyHolidaysAdapter
import com.tmotions.wms.api.Api
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.common.MyApplication
import com.tmotions.wms.common.MyApplication.Companion.TAG
import com.tmotions.wms.common.Token
import com.tmotions.wms.common.Utility
import com.tmotions.wms.databinding.ActivityCompanyHolidaysBinding
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.HolidayCalenderDataModel
import com.tmotions.wms.models.HolidayCalenderModel
import com.tmotions.wms.models.HolidayModel
import es.dmoral.toasty.Toasty
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class ActivityCompanyHolidays : AppCompatActivity(),View.OnClickListener,ItemClickListener {

    lateinit var binding: ActivityCompanyHolidaysBinding
    var holidayList = ArrayList<HolidayCalenderDataModel>()
    var month: GregorianCalendar? = null
    var itemmonth: GregorianCalendar? = null
    val arrlist_calendar = ArrayList<HolidayModel>()

    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private var readExternalStorage: String? = null
    private var readMediaImages: String? = null
    private var fileName = "WMS_holiday_calender"
    var storage_permissions = Manifest.permission.READ_EXTERNAL_STORAGE.also {
        readExternalStorage = it
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storage_permissions_33 = Manifest.permission.READ_MEDIA_IMAGES.also {
        readMediaImages = it
    }

    fun permissions(): Array<String>? {
        val p: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        return arrayOf(p)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_company_holidays)
        binding.headerLayout.txtTitle.text = "Company Holidays"

        Utility.setActionBar(this,window)
        val toolbar: Toolbar = binding.headerLayout.toolbar
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }

        Utility.sendViewScreenEvent("CompanyHoliday_Activity_Android",this)

        month = GregorianCalendar.getInstance() as GregorianCalendar
        itemmonth = month!!.clone() as GregorianCalendar
        var adapter = CalendarAdapter(this, month, arrlist_calendar)

        binding.headerLayout.imgBack.setOnClickListener(this)

       if(Utility.isOnline(this)) {
           getHolidayList()
       }
        else
            Utility.displayAlertDialog(
                this@ActivityCompanyHolidays,
                resources.getString(R.string.nointernet),
                layoutInflater
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
            finish()
             true
            }
            R.id.action_holiday -> {
                Utility.sendActionEvent("HolidayCalender_ImageButton_Android",this)
               /* if(downloadFileCheck (fileName))
                    openPDF(fileName)
                else {
                    if (Utility.isOnline(this)) {
                        //  checkStoragePermission()
                        permissions()?.let {
                            ActivityCompat.requestPermissions(
                                this@ActivityCompanyHolidays,
                                it,
                                REQUEST_CODE_STORAGE_PERMISSION
                            )
                        };
                    } else
                        Utility.displayAlertDialog(
                            this, resources.getString(R.string.nointernet),
                            layoutInflater
                        )
                }*/
                if (Utility.isOnline(this)) {
                    //  checkStoragePermission()
                    permissions()?.let {
                        ActivityCompat.requestPermissions(
                            this@ActivityCompanyHolidays,
                            it,
                            REQUEST_CODE_STORAGE_PERMISSION
                        )
                    };
                } else
                    Utility.displayAlertDialog(
                        this, resources.getString(R.string.nointernet),
                        layoutInflater
                    )
                    true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.holiday_menu, menu)
        return true
    }

    private fun getHolidayList() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@ActivityCompanyHolidays)
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService : Call<HolidayCalenderModel> = retrofitApiInterface.getHolidayList(loginModel.access_Token)

        mService.enqueue(object : Callback<HolidayCalenderModel> {
            override fun onResponse(call: Call<HolidayCalenderModel>, response: Response<HolidayCalenderModel>) {
//                binding.shimmerFrameLayout.visibility = View.GONE
//                binding.shimmerFrameLayout.stopShimmer()
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        val holidayCalenderModel = response.body()
                        if (holidayCalenderModel!!.statusCode == 200) {
                            binding.shimmerFrameLayout.visibility = View.GONE
                            holidayList = holidayCalenderModel.data
                            if(holidayList.size > 0){
                                val layoutManager = LinearLayoutManager(this@ActivityCompanyHolidays, LinearLayoutManager.VERTICAL, false)
                                binding.recyclerViewCompanyHolidays.layoutManager = layoutManager
                                val companyHolidaysAdapter = CompanyHolidaysAdapter(this@ActivityCompanyHolidays, holidayList)
                                companyHolidaysAdapter.setItemClickListener(this@ActivityCompanyHolidays)
                                binding.recyclerViewCompanyHolidays.adapter = companyHolidaysAdapter
                                binding.recyclerViewCompanyHolidays.visibility = View.VISIBLE
                                binding.routNoData.visibility = View.GONE
                            }
                            else{
                                binding.routNoData.visibility = View.VISIBLE
                                binding.recyclerViewCompanyHolidays.visibility = View.GONE
                                binding.shimmerFrameLayout.visibility = View.GONE
                            }
                        } else {
                            if (holidayCalenderModel.statusCode == 401)
                                Token.getRefreshToken(
                                    this@ActivityCompanyHolidays,
                                    object : RetrofitResponseListener {
                                        override fun onSuccess() {
                                            getHolidayList()
                                        }

                                        override fun onFailure() {
                                            binding.shimmerFrameLayout.visibility = View.GONE
                                        }
                                    })
                            else {
                                binding.shimmerFrameLayout.visibility = View.GONE
                                Utility.displayAlertDialog(
                                    this@ActivityCompanyHolidays,
                                    holidayCalenderModel.message,
                                    layoutInflater
                                )
                            }
                        }
                    }
                    else {
                        binding.shimmerFrameLayout.visibility = View.GONE
                        Utility.displayAlertDialog(
                            this@ActivityCompanyHolidays,
                            response.message(),
                            layoutInflater
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<HolidayCalenderModel>, t: Throwable) {
                Utility.setWindowClickable(window)
                Utility.displayAlertDialog(this@ActivityCompanyHolidays, "$t", layoutInflater)
                binding.shimmerFrameLayout.visibility = View.GONE
            }
        })
    }

    private fun getHolidayCalenderPdf() {
        Utility.setWindowNotClickable(window)
        val loginModel = Login_Helper.getLogin(this@ActivityCompanyHolidays)
        binding.mprogress.visibility = View.VISIBLE
        val retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)
        val mService: Call<ResponseBody> = retrofitApiInterface.getHolidayCalenderPdf(loginModel.access_Token)

        mService.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Utility.setWindowClickable(window)
                try {
                    if (response.isSuccessful) {
                        response.body()?.let { saveToDisk(it,fileName) }
                    }
                    else {
                        Toasty.error(this@ActivityCompanyHolidays,resources.getString(R.string.something_went_wrong)+" ${response.message()}").show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utility.setWindowClickable(window)
                binding.mprogress.visibility = View.VISIBLE
                Toasty.error(this@ActivityCompanyHolidays,resources.getString(R.string.something_went_wrong)+" $t").show()
            }
        })
    }

    private fun saveToDisk(body: ResponseBody, filename: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val uniqueFilename = "${filename}_$timestamp.pdf"
            val destinationFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                uniqueFilename
            )
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                `is` = body.byteStream()
                os = FileOutputStream(destinationFile)
                val data = ByteArray(4096)
                var count: Int
                while (`is`.read(data).also { count = it } != -1) {
                    os.write(data, 0, count)
                }
                os.flush()
                binding.mprogress.visibility = View.GONE
                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_LONG).show()
                openPDF(destinationFile)
                return
            } catch (e: IOException) {
                e.printStackTrace()
                binding.mprogress.visibility = View.GONE
                Toast.makeText(this, "Failed to save the file!", Toast.LENGTH_LONG).show()
                return
            } finally {
                `is`?.close()
                os?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            binding.mprogress.visibility = View.GONE
            Toast.makeText(this, "Failed to save the file!", Toast.LENGTH_LONG).show()
            return
        }
    }
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> {
                finish()
            }
        }
    }

    private fun openPDF(file: File) {
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uri)
        pdfOpenIntent.setDataAndType(uri, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG).show()
        }
    }
    override fun onClick(view: View?, position: Int) {
        Utility.sendActionEvent("HolidayNameClick_Android",this)
        val holidayModel = holidayList[position]
        if(!holidayModel.isDisabled) {
            val intent = Intent(this, ApplyLeaveActivity::class.java)
            intent.putExtra("leaveType", "Restricted Holiday")
            intent.putExtra("displayDate", "")
            intent.putExtra("sendDate", "")
            intent.putExtra("holidayName", holidayModel.holiday)
            startActivity(intent)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("location3", "location code : $requestCode")
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    //proceedAfterPermission() // permission was granted.
                    Log.e("location3", "location granted")

//                    if (applictionFile.exists())
//                        applictionFile.delete()

                        getHolidayCalenderPdf()


//                    if (Build.VERSION.SDK_INT >= 30) {
//                        if (!Environment.isExternalStorageManager()) {
//                            var getpermission = Intent()
//                            getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                            startActivity(getpermission);
//                        } else
//                            getHolidayCalenderPdf()
//                    }
//                    else
//                        getHolidayCalenderPdf()
                } else {
                    // permission denied.
                    Log.e("location3", "location denied")
                }
                return
            }
        }
    }

    private fun downloadFileCheck(fileName: String): Boolean {
        var fileCheck = false
        val applictionFile = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + fileName
        )
        if (applictionFile.exists()) {
            applictionFile.delete()

//            Toast.makeText(
//                applicationContext, "File Already Exists",
//                Toast.LENGTH_LONG
//            ).show()
            fileCheck = true
        }
  //      else {
//            val servicestring = DOWNLOAD_SERVICE
//            val downloadmanager: DownloadManager
//            downloadmanager = getSystemService(servicestring) as DownloadManager
//            val uri = Uri.parse(DownloadUrl)
//            val request = DownloadManager.Request(uri)
//            request.setDestinationInExternalFilesDir(
//                this@MainActivity,
//                Environment.DIRECTORY_DOWNLOADS, "mysongs.mp3"
//            )
//            val reference = downloadmanager.enqueue(request)
      //  }
        return fileCheck
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        return try {
            // todo change the file location/name according to your needs

            val destinationFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
//            val futureStudioIconFile =
//                File((getExternalFilesDir(null) + File.separator).toString() + fileName)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destinationFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    if (outputStream != null) {
                        outputStream.write(fileReader, 0, read)
                    }
                    fileSizeDownloaded += read.toLong()
                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
                }
                if (outputStream != null) {
                    outputStream.flush()
                }
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }
}