package com.tmotions.wms.listners

class NotificationReceivedListener {

    var listener: OnNotificationReceivedListener? = null

    interface OnNotificationReceivedListener{
        fun onNotificationReceived(bundle: String?)
    }

    fun setOnNotificationReceivedListener(param: OnNotificationReceivedListener) {
        listener = param
    }

    fun notificationReceived(bundle: String?){
        listener?.let {
            it.onNotificationReceived(bundle)
        }
    }
}