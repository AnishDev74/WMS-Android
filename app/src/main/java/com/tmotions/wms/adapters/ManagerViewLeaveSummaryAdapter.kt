package com.tmotions.wms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemCheckedListener
import com.tmotions.wms.listners.ManagerViewItemClickListener
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary

class ManagerViewLeaveSummaryAdapter(
    var context: Context,
    arrayListLeaveSummary: ArrayList<LeaveSummary>,
    allCheck: Boolean
) : RecyclerView.Adapter<ManagerViewLeaveSummaryAdapter.ComplaintHolder>() {
    private var arrayListLeaveSummary: ArrayList<LeaveSummary>
    private var itemObjectListener: ManagerViewItemClickListener? = null
    private var itemCheckedListener: ItemCheckedListener? = null
    private var allCheck: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.manager_leave_summary_item,
            parent,
            false
        )
        return ComplaintHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListLeaveSummary.size
    }

    fun getItem(position: Int): LeaveSummary {
        return arrayListLeaveSummary[position]
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtEmpName: TextView
        var txtLeaveType: TextView
        var txtLeaveDate: TextView
        var loutRoot: LinearLayout
        var imgCheck: ImageView
        var imgDelete: ImageView
        var imgInfo: ImageView
        var chkBoxItem: CheckBox

        init {
            txtEmpName = itemView.findViewById<View>(R.id.txtEmpName) as TextView
            txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
            txtLeaveDate = itemView.findViewById<View>(R.id.txtLeaveDate) as TextView
            imgCheck = itemView.findViewById<View>(R.id.imgCheck) as ImageView
            imgDelete = itemView.findViewById<View>(R.id.imgDelete) as ImageView
            imgInfo = itemView.findViewById<View>(R.id.imgInfo) as ImageView
            loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout
            chkBoxItem = itemView.findViewById<View>(R.id.chkBoxItem) as CheckBox
        }
    }

    fun setItemClickListener(itemObjectListener: ManagerViewItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    fun setItemCheckedListener(itemCheckedListener: ItemCheckedListener?) {
        this.itemCheckedListener = itemCheckedListener
    }

    init {
        this.arrayListLeaveSummary = arrayListLeaveSummary
        this.allCheck = allCheck
    }

    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {
    
        val leaveModel = getItem(position)
        try {

            holder.txtLeaveType.text = leaveModel.leaveType
            holder.txtLeaveDate.text = leaveModel.leaveDay
            holder.txtEmpName.text = leaveModel.employeeName
            holder.chkBoxItem.isChecked = leaveModel.isChecked

            if(position%2==0)
                holder.loutRoot.setBackgroundColor(Color.parseColor("#F2F2F2"))
            else
                holder.loutRoot.setBackgroundColor(Color.parseColor("#FFFFFF"))

            if(leaveModel.leaveType.equals("CO") || leaveModel.leaveType.equals("Sal")) {
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtEmpName.setTextColor(ContextCompat.getColor(context, R.color.overtime))
            }
            if(leaveModel.leaveCategory.equals("Overtime Cancel") || leaveModel.leaveCategory.equals("Leave Cancel") || leaveModel.leaveCategory.equals("WFH Cancel")){
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
                holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
                holder.txtEmpName.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
            }

           holder.imgCheck.setOnClickListener {
                if (itemObjectListener != null)
                    itemObjectListener!!.onClick("Approved",position)
            }
            holder.imgDelete.setOnClickListener {
                if (itemObjectListener != null) itemObjectListener!!.onClick(
                    "Cancelled",
                    position
                )
            }
            holder.imgInfo.setOnClickListener {
                if (itemObjectListener != null) itemObjectListener!!.onClick(
                    "Info",
                    position
                )
            }

            if(allCheck){
                holder.imgCheck.isClickable = false
                holder.imgDelete.isClickable = false
                holder.imgInfo.isClickable = false
                holder.imgCheck.alpha = 0.2f
                holder.imgDelete.alpha = 0.2f
                holder.imgInfo.alpha = 0.2f
            }
            else{
                holder.imgCheck.isClickable = true
                holder.imgDelete.isClickable = true
                holder.imgInfo.isClickable = true
                holder.imgCheck.alpha = 1f
                holder.imgDelete.alpha = 1f
                holder.imgInfo.alpha = 1f
            }

            if(leaveModel.isChecked){
                holder.imgCheck.isClickable = false
                holder.imgDelete.isClickable = false
                holder.imgInfo.isClickable = false
            }
            else{
                holder.imgCheck.isClickable = true
                holder.imgDelete.isClickable = true
                holder.imgInfo.isClickable = true

            }

            holder!!.chkBoxItem.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) {
                    if (itemCheckedListener != null) itemCheckedListener!!.onChecked(position, true)
                    holder.imgCheck.isClickable = false
                    holder.imgDelete.isClickable = false
                    holder.imgInfo.isClickable = false
                    holder.imgCheck.alpha = 0.2f
                    holder.imgDelete.alpha = 0.2f
                    holder.imgInfo.alpha = 0.2f
                }
                else {
                    if (itemCheckedListener != null) itemCheckedListener!!.onChecked(position,false )
                    holder.imgCheck.isClickable = true
                    holder.imgDelete.isClickable = true
                    holder.imgInfo.isClickable = true
                    holder.imgCheck.alpha = 1f
                    holder.imgDelete.alpha = 1f
                    holder.imgInfo.alpha = 1f
                }
            })

//            holder.chkBoxItem.setOnClickListener(View.OnClickListener {
//                if (holder.chkBoxItem.isChecked())
//                    holder.chkBoxItem.setChecked(
//                    false
//                ) else holder.chkBoxItem.setChecked(true)
//            })
        }
        catch (e:Exception){
            e.printStackTrace()
      }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
