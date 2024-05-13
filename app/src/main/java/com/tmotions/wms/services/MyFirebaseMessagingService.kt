package com.tmotions.wms.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tmotions.wms.R
import com.tmotions.wms.activities.SplashActivity
import com.tmotions.wms.listners.NotificationReceivedListener
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var notificationManager: NotificationManager? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("onMessageReceived", "onMessageReceived calling ")

        val notificationIntent = Intent(this, SplashActivity::class.java) //HomeActivity
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */, notificationIntent,
            flag)

        //You should use an actual ID instead
        val notificationId = Random().nextInt(60000)
        if (remoteMessage.notification != null) {
//            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val r = RingtoneManager.getRingtone(this, defaultSoundUri)
//            r.play()
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels()
            }
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                notificationBuilder.setSmallIcon(R.mipmap.notification_trans)
                notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
            } else {
              //  notificationBuilder.setSmallIcon(R.mipmap.notification)
                val bigText = NotificationCompat.BigTextStyle()
                bigText.setBigContentTitle(remoteMessage.notification!!.title)
                bigText.setSummaryText(remoteMessage.notification!!.body)
                notificationBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
                notificationBuilder.setSmallIcon(R.mipmap.notification_trans)
            }
       //     notificationBuilder.setColorized(true)
            notificationBuilder.setContentTitle(remoteMessage.notification!!.title)
            notificationBuilder.setContentText(remoteMessage.notification!!.body)
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.notification!!.body))
            notificationBuilder.priority = Notification.DEFAULT_ALL
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
          //  notificationBuilder.setSmallIcon(R.mipmap.notification)
            notificationBuilder.setContentIntent(pendingIntent)
            notificationManager!!.notify(notificationId, notificationBuilder.build())
            notificationReceivedListener.notificationReceived("okay")
        }
    }

    fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("awesome", "Error in getting notification image: " + e.localizedMessage)
            null
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels() {
        val adminChannelName: CharSequence = getString(R.string.admin_channel_name)
        val adminChannelDescription = getString(R.string.admin_channel_description)
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_LOW
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        adminChannel.enableVibration(true)
        if (notificationManager != null) {
            notificationManager!!.createNotificationChannel(adminChannel)
        }
    }

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.mipmap.notification else R.mipmap.notification
    }

    companion object {
        var notificationReceivedListener = NotificationReceivedListener()
        private const val ADMIN_CHANNEL_ID = "admin_channel"
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }
}