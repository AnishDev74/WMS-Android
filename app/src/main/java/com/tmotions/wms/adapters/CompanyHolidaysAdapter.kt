package com.tmotions.wms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.models.HolidayCalenderDataModel

class CompanyHolidaysAdapter(var context: Context, arrayListHolidayCalender: ArrayList<HolidayCalenderDataModel>) : RecyclerView.Adapter<CompanyHolidaysAdapter.ComplaintHolder>() {
    private var arrayListHolidayCalender: ArrayList<HolidayCalenderDataModel>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.company_holidays_item_list,
            parent,
            false
        )
        return ComplaintHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListHolidayCalender.size

    }

    fun getItem(position: Int): HolidayCalenderDataModel {
        return arrayListHolidayCalender[position]
    }

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var loutRoot : LinearLayout
        var txtDate: TextView
        var txtLeaveName: TextView
        var txtLeaveType: TextView

        init {
            txtDate = itemView.findViewById<View>(R.id.txtDate) as TextView
            txtLeaveName = itemView.findViewById<View>(R.id.txtLeaveName) as TextView
            txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
            loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout

        }
    }

    fun setItemClickListener(itemObjectListener: ItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    init {
        this.arrayListHolidayCalender = arrayListHolidayCalender
    }

    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {
        if(position%2==0)
            holder.loutRoot.setBackgroundColor(Color.parseColor("#F2F2F2"))
        else
            holder.loutRoot.setBackgroundColor(Color.parseColor("#FFFFFF"))

        val holidayCalenderModel = getItem(position)

        try {

            holder.txtDate.text = holidayCalenderModel.date
            holder.txtLeaveName.text = holidayCalenderModel.holiday
            holder.txtLeaveType.text = holidayCalenderModel.holidayType

            if(holidayCalenderModel.holidayType.equals("Restricted"))
                holder.txtLeaveType.setTextColor(Color.parseColor("#00A940"))
            else
                holder.txtLeaveType.setTextColor(Color.parseColor("#E36D02"))

            if(holidayCalenderModel.holidayType.equals("Restricted") && holidayCalenderModel.isDisabled)
                holder.txtLeaveType.setTextColor(Color.parseColor("#A8A8A8"))

            holder.txtLeaveType.setOnClickListener {
                if (holidayCalenderModel.holidayType.equals("Restricted")) {
                    if (itemObjectListener != null) itemObjectListener!!.onClick(
                        holder.txtLeaveType,
                        position
                    )
                }
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
