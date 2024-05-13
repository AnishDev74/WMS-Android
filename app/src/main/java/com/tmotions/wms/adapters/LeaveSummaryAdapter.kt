package com.tmotions.wms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary

class LeaveSummaryAdapter(var context: Context, arrayListProduct: ArrayList<LeaveSummary>) : RecyclerView.Adapter<LeaveSummaryAdapter.ComplaintHolder>() {
    private var arrayListProduct: ArrayList<LeaveSummary>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.leave_summary_item,parent,false)
        return ComplaintHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListProduct.size
      }

    fun getItem(position: Int): LeaveSummary {
        return arrayListProduct[position]
    }

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLeaveType: TextView
        var txtLeaveDate: TextView
        var txtLeaveStatus: TextView
        var imgViewDetails: ImageView
        var loutRoot: LinearLayout

        init {
            txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
            txtLeaveDate = itemView.findViewById<View>(R.id.txtLeaveDate) as TextView
            txtLeaveStatus = itemView.findViewById<View>(R.id.txtLeaveStatus) as TextView
            imgViewDetails = itemView.findViewById<View>(R.id.imgViewDetails) as ImageView
            loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout
        }
    }

    fun setItemClickListener(itemObjectListener: ItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    init {
        this.arrayListProduct = arrayListProduct
    }

    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {
    
        val leaveModel = getItem(position)
        try {
            holder.txtLeaveType.text = leaveModel.leaveType
            holder.txtLeaveDate.text = leaveModel.leaveDay
            holder.txtLeaveStatus.text = leaveModel.leaveStatus

            if(position%2==0)
                holder.loutRoot.setBackgroundColor(Color.parseColor("#F2F2F2"))
            else
                holder.loutRoot.setBackgroundColor(Color.parseColor("#FFFFFF"))

            if(leaveModel.leaveStatus.equals("Pending"))
            holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.pending))
           else if(leaveModel.leaveStatus.equals("Cancelled"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.cancelled))
            else if(leaveModel.leaveStatus.equals("Rejected"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.rejected))
            else if(leaveModel.leaveStatus.equals("Disapproved"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.disapproved))
            else if(leaveModel.leaveStatus.equals("Withdrawn"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.withdrawn))
            else if(leaveModel.leaveStatus.equals("Approved"))
                holder.txtLeaveStatus.setTextColor(ContextCompat.getColor(context, R.color.approved))

            holder.imgViewDetails.setOnClickListener {
                if (itemObjectListener != null) itemObjectListener!!.onClick(
                    holder.imgViewDetails,
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
