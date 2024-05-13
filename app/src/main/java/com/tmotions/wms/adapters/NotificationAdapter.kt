package com.tmotions.wms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.models.notificationmodels.NotificationList
import java.util.*

class NotificationAdapter(var context: Context, arrayListProduct: ArrayList<NotificationList>) : RecyclerView.Adapter<NotificationAdapter.ComplaintHolder>() {
    private var arrayListNotification: ArrayList<NotificationList>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.notification_list_items,
            parent,
            false
        )
        return ComplaintHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListNotification.size

    }

    fun getItem(position: Int): NotificationList {
        return arrayListNotification[position]
    }

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var loutRoot : LinearLayout
        var txtShortName: TextView
        var txtNotification: TextView
        var txtTime: TextView

        init {
          loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout
            txtShortName = itemView.findViewById<View>(R.id.txtShortName) as TextView
            txtNotification = itemView.findViewById<View>(R.id.txtNotification) as TextView
            txtTime = itemView.findViewById<View>(R.id.txtTime) as TextView
        }
    }

    fun setItemClickListener(itemObjectListener: ItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    init {
        this.arrayListNotification = arrayListProduct
    }

    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {

        val notificationListModel = getItem(position)
       try {

           holder.txtShortName.text = notificationListModel.sender
           holder.txtNotification.text = notificationListModel.notification
           holder.txtTime.text = notificationListModel.time

           if(notificationListModel.IsNotificationView)
               holder.loutRoot.setBackgroundColor(Color.parseColor("#DBDBDB"))

           holder.loutRoot.setOnClickListener {
               holder.loutRoot.setBackgroundColor(Color.parseColor("#DBDBDB"))
               if (itemObjectListener != null) itemObjectListener!!.onClick(
                   holder.loutRoot,
                   position
               )
           }
       }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
