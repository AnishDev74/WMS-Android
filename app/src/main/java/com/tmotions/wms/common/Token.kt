package com.tmotions.wms.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.tmotions.wms.R
import com.tmotions.wms.api.Api
import com.tmotions.wms.activities.SplashActivity
import com.tmotions.wms.api.RetrofitClient
import com.tmotions.wms.dbhelper.Login_Helper
import com.tmotions.wms.listners.RetrofitResponseListener
import com.tmotions.wms.models.LoginModel
import com.tmotions.wms.models.tokenexpiremodel.TokenExpireModel
import es.dmoral.toasty.Toasty


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object Token {

    var retrofitApiInterface: Api? = null
 
    fun getRefreshToken(context: Context, retrofitResponseListener: RetrofitResponseListener) {

        var loginmodel = Login_Helper.getLogin(MyApplication.instance!!)

        var loginModel = LoginModel()
        loginModel.access_Token =  loginmodel.access_Token
        loginModel.refresh_Token =  loginmodel.refresh_Token

        retrofitApiInterface = RetrofitClient.getClient(MyApplication.instance).create(Api::class.java)

        val mService: Call<TokenExpireModel> = retrofitApiInterface!!.refreshToken(loginmodel.access_Token,loginModel)

        mService.enqueue(object : Callback<TokenExpireModel> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<TokenExpireModel>, response: Response<TokenExpireModel>) {
                try {
                    if (response.isSuccessful) {
                        val tokenExpireModel = response.body()
                        if (tokenExpireModel!!.statusCode == 200) {
                            var tokenDataModel = tokenExpireModel.data

                            Login_Helper.updateAccessAndRefreshToken(
                                MyApplication.instance!!,
                                loginmodel.userId.toString(),
                                "Bearer "+tokenDataModel.access_Token,
                                tokenDataModel.refresh_Token
                            )
                            retrofitResponseListener.onSuccess()
                        } else {
                           // Toasty.error(context,"authentication failed: " + tokenExpireModel.message).show()
                            Toasty.error(context,"You token has been expired. Please login again").show()
                            retrofitResponseListener.onFailure()
                            Login_Helper.signOut(context)
                            val intent = Intent(context, SplashActivity::class.java)
                            context.startActivity(intent)
                            (context as Activity).finish()
                        }
                    }
                    else {
                       // Toasty.error(context,"authentication failed: "+response.message()).show()
                        Toasty.error(context,"You token has been expired. Please login again").show()
                        retrofitResponseListener.onFailure()
                        Login_Helper.signOut(context)
                        val intent = Intent(context, SplashActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<TokenExpireModel>, t: Throwable) {
                Toasty.error(context,"You token has been expired. Please login again").show()
            //    Toasty.error(context,"authentication failed: $t").show()
                retrofitResponseListener.onFailure()
                Login_Helper.signOut(context)
                val intent = Intent(context, SplashActivity::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }
        })
    }
}