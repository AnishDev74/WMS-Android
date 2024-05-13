package com.tmotions.wms.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.tmotions.wms.R
import com.tmotions.wms.models.restrictedholidaymodels.RestrictedDataListModel
import java.util.ArrayList

class RestrictedDropDownAdapter(context: Context, arrayListRestricted: ArrayList<RestrictedDataListModel>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater
    private val arrayListRestricted: ArrayList<RestrictedDataListModel>

    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.arrayListRestricted = arrayListRestricted
    }

    override fun getCount(): Int {
        return arrayListRestricted.size
    }

    override fun getItem(position: Int): Any {
        return arrayListRestricted[position]
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

        val restrictedHolidayModel = arrayListRestricted[position]
        vh.txtItemName.text = restrictedHolidayModel.holiday
        return view

    }

    private class ListRowHolder(row: View?) {
        val txtItemName: TextView

        init {
            this.txtItemName = row?.findViewById(R.id.txtItemName) as TextView
         }
    }
}