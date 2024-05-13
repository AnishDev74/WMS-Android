package com.tmotions.wms.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tmotions.wms.R
import com.tmotions.wms.listners.ItemClickListener
import com.tmotions.wms.models.dashboardModels.LeaveBalanceModel

class EmpLeaveAdapter(var context: Context, arrayListLeave: ArrayList<LeaveBalanceModel>) : RecyclerView.Adapter<EmpLeaveAdapter.ComplaintHolder>() {
    private var arrayListLeave: ArrayList<LeaveBalanceModel>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.emp_leave_item,
            parent,
            false
        )
        return ComplaintHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayListLeave.size

    }

    fun getItem(position: Int): LeaveBalanceModel {
        return arrayListLeave[position]
    }

    inner class ComplaintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLeaveCount: TextView
        var txtLeaveType: TextView
        var txtAvailableGrant: TextView
        var txtCarryForword: TextView
        var btnApply: Button
        var viewLine: View

        init {
            txtLeaveCount = itemView.findViewById<View>(R.id.txtLeaveCount) as TextView
            txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
            txtAvailableGrant = itemView.findViewById<View>(R.id.txtAvailableGrant) as TextView
            txtCarryForword = itemView.findViewById<View>(R.id.txtCarryForword) as TextView
            btnApply = itemView.findViewById<View>(R.id.btnApply) as Button
            viewLine = itemView.findViewById(R.id.viewLine) as View
        }
    }

    fun setItemClickListener(itemObjectListener: ItemClickListener?) {
        this.itemObjectListener = itemObjectListener
    }

    init {
        this.arrayListLeave = arrayListLeave
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: ComplaintHolder, position: Int) {
        val leaveBalanceModel = getItem(position)
        try {

            holder.txtLeaveCount.text = leaveBalanceModel.balance.toString()
            holder.txtLeaveType.text = leaveBalanceModel.leaveType.toString() +" (" +leaveBalanceModel.leaveAbbreviation.toString()+")"
            holder.txtAvailableGrant.text = "Granted ("+leaveBalanceModel.granted.toString()+")"+" Availed ("+leaveBalanceModel.availed.toString()+")"
            holder.txtCarryForword.text = "Carry Forwarded ("+leaveBalanceModel.carryForwarded.toString() +")"
            if(leaveBalanceModel.leaveAbbreviation.equals("EL"))
                holder.txtCarryForword.text = "Carry Forwarded ("+leaveBalanceModel.carryForwarded.toString() +")"
            else
            holder.txtCarryForword.visibility = View.GONE

            if(leaveBalanceModel.leaveAbbreviation.equals("EL")){
                holder.txtLeaveCount.setTextColor(ContextCompat.getColor(context, R.color.el))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.el))
                holder.viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.el))
            }
            else  if(leaveBalanceModel.leaveAbbreviation.equals("CL")){
                holder.txtLeaveCount.setTextColor(ContextCompat.getColor(context, R.color.cl))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.cl))
                holder.viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.cl))
            }else  if(leaveBalanceModel.leaveAbbreviation.equals("SL")){
                holder.txtLeaveCount.setTextColor(ContextCompat.getColor(context, R.color.sl))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.sl))
                holder.viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.sl))
            }else  if(leaveBalanceModel.leaveAbbreviation.equals("CO")){
                holder.txtLeaveCount.setTextColor(ContextCompat.getColor(context, R.color.co))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.co))
                holder.viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.co))
            }else  if(leaveBalanceModel.leaveAbbreviation.equals("RH")){
                holder.txtLeaveCount.setTextColor(ContextCompat.getColor(context, R.color.rh))
                holder.txtLeaveType.setTextColor(ContextCompat.getColor(context, R.color.rh))
                holder.viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.rh))
            }

            holder.btnApply.setOnClickListener {
                if (itemObjectListener != null) itemObjectListener!!.onClick(
                    holder.btnApply,
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
