package com.tmotions.wms.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.tmotions.wms.R
import com.tmotions.wms.models.managerview.ResourceList
import java.util.ArrayList

class ManagerResourceListDropDownAdapter(context: Context, myRequestArr: ArrayList<ResourceList>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater
    private val myRequestArr: ArrayList<ResourceList>
    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.myRequestArr = myRequestArr
    }

    override fun getCount(): Int {
        return myRequestArr.size
    }

    override fun getItem(position: Int): Any {
        return myRequestArr[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val vh: ListRowHolder
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.dropdown_item, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }

        val dropDownList = myRequestArr[position]
        vh.txtItemName.text = dropDownList.tm_employee_name
        return view

    }

    private class ListRowHolder(row: View?) {
        val txtItemName: TextView

        init {
            this.txtItemName = row?.findViewById(R.id.txtItemName) as TextView
         }
    }
}