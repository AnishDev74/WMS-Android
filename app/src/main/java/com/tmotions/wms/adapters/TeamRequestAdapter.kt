package com.tmotions.wms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary

class TeamRequestAdapter(var context: Context, arrayListLeaveSummary: ArrayList<LeaveSummary>) : RecyclerView.Adapter<TeamRequestAdapter.ComplaintHolder>() {
    private var arrayListLeaveSummary: ArrayList<LeaveSummary>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.team_request_item_list,
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

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtName: TextView
        var txtLeaveType: TextView
        var txtLeaveDate: TextView
        var txtLeaveStatus: TextView
        var loutRoot: LinearLayout
        init {
            txtName = itemView.findViewById<View>(R.id.txtName) as TextView
            txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
            txtLeaveDate = itemView.findViewById<View>(R.id.txtLeaveDate) as TextView
            txtLeaveStatus = itemView.findViewById<View>(R.id.txtLeaveStatus) as TextView
            loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout
        }
    }

    fun setItemClickListener(itemObjectListener: ItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    init {
        this.arrayListLeaveSummary = arrayListLeaveSummary
    }

    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {
        if(position%2==0)
            holder.loutRoot.setBackgroundColor(Color.parseColor("#F2F2F2"))
        else
            holder.loutRoot.setBackgroundColor(Color.parseColor("#FFFFFF"))

        val leaveSummaryModel = getItem(position)
        try {

            holder.txtName.text = leaveSummaryModel.employeeName
            holder.txtLeaveType.text = leaveSummaryModel.leaveType
            holder.txtLeaveDate.text = leaveSummaryModel.leaveDay
            holder.txtLeaveStatus.text = leaveSummaryModel.leaveStatus

            if(leaveSummaryModel.leaveCategory.equals("Leave Cancel") && (leaveSummaryModel.leaveStatus.equals("Approved") ||
                        leaveSummaryModel.leaveStatus.equals("Rejected") || leaveSummaryModel.leaveStatus.equals("Cancelled")) ) {
                holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
                holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
            }
            else if(leaveSummaryModel.leaveType.equals("Sal") && leaveSummaryModel.leaveCategory.equals("Overtime") ){
                holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.overtime))
            }
            else if(leaveSummaryModel.leaveType.equals("CO") && leaveSummaryModel.leaveCategory.equals("Overtime") ){
                holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.overtime))
                holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.overtime))
            }

//                if(leaveSummaryModel.leaveStatus.equals("Cancelled") || leaveSummaryModel.leaveStatus.equals("Rejected") || leaveSummaryModel.leaveStatus.equals("Approved")){
//                    holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
//                    holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
//                    holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
//                }
//                else if(leaveSummaryModel.leaveType.equals("CO") || leaveSummaryModel.leaveType.equals("Sal")){
//                    holder.txtName.setTextColor(ContextCompat.getColor(context, R.color.overtime))
//                    holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.overtime))
//                    holder.txtLeaveDate.setTextColor(ContextCompat.getColor(context, R.color.overtime))
//                }

            if(leaveSummaryModel.leaveStatus.equals("Pending"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.pending))
            else if(leaveSummaryModel.leaveStatus.equals("Cancelled"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.cancelled))
            else if(leaveSummaryModel.leaveStatus.equals("Rejected"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.rejected))
            else if(leaveSummaryModel.leaveStatus.equals("Disapproved"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
            else if(leaveSummaryModel.leaveStatus.equals("Withdrawn"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.withdrawn))
            else if(leaveSummaryModel.leaveStatus.equals("Approved"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.approved))

            holder.loutRoot.setOnClickListener {
                if (itemObjectListener != null) itemObjectListener!!.onClick(
                    holder.txtLeaveStatus,
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
