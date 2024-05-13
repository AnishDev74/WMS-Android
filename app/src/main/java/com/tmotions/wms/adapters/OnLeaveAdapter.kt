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
import com.tmotions.wms.models.leaveSummaryModels.LeaveSummary
import java.util.*

class OnLeaveAdapter(var context: Context, arrayListLeaveSummary: ArrayList<LeaveSummary>) : RecyclerView.Adapter<OnLeaveAdapter.ComplaintHolder>() {
    private var arrayListLeaveSummary: ArrayList<LeaveSummary>
    private var itemObjectListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintHolder {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.actvitiy_on_leave_items,
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
        var loutRoot : LinearLayout
        var txtEmpName: TextView
        var txtLeaveType: TextView

        init {
          loutRoot = itemView.findViewById<View>(R.id.loutRoot) as LinearLayout
          txtEmpName = itemView.findViewById<View>(R.id.txtEmpName) as TextView
          txtLeaveType = itemView.findViewById<View>(R.id.txtLeaveType) as TextView
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

        val leaveModel = getItem(position)
       try {

           holder.txtEmpName.text = leaveModel.employeeName
           holder.txtLeaveType.text = leaveModel.leaveType

       }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
