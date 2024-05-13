package com.tmotions.wms.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tmotions.wms.R;
import com.tmotions.wms.common.Utility;
import com.tmotions.wms.models.HolidayModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends BaseAdapter {
    private Context mContext;
    private java.util.Calendar month;
    public GregorianCalendar pmonth;
    public GregorianCalendar pmonthmaxset;
    private GregorianCalendar selectedDate;
    int firstDay;
    int maxWeeknumber;
    int maxP;
    int calMaxP;
    int mnthlength;
    String itemvalue, curentDateString;
    DateFormat df;

    private ArrayList<String> items;
    private ArrayList<HolidayModel> typeList;
    public static List<String> dayString;
    private View previousView;

    public CalendarAdapter(Context c, GregorianCalendar monthCalendar, ArrayList<HolidayModel> typeList) {
        CalendarAdapter.dayString = new ArrayList<String>();
        Locale.setDefault(Locale.US);
        month = monthCalendar;
        selectedDate = (GregorianCalendar) monthCalendar.clone();
        mContext = c;
        month.set(GregorianCalendar.DAY_OF_MONTH, 1);
        this.items = new ArrayList<String>();
        this.typeList = typeList;
       // df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        df = new SimpleDateFormat("yyyy-MM-dd");
        curentDateString = df.format(selectedDate.getTime());
        refreshDays();
    }


    public void setItems(ArrayList<String> items) {
        for (int i = 0; i != items.size(); i++) {
            if (items.get(i).length() == 1) {
                items.set(i, "0" + items.get(i));
            }
        }
        this.items = items;
    }


    public int getCount() {
        return dayString.size();
    }

    public Object getItem(int position) {
        return dayString.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced By the Adapter  
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;
        LinearLayout lout_top;
        if (convertView == null) { // if it's not recycled, initialize some  
            // attributes  
            LayoutInflater vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);

        }
        dayView = (TextView) v.findViewById(R.id.date);
        lout_top = (LinearLayout) v.findViewById(R.id.lout_top);
        // separates dayString into parts.
        String[] separatedTime = dayString.get(position).split("-");
        // taking last part of date. ie; 2 from 2012-12-02  
        String gridvalue = separatedTime[2].replaceFirst("^0*", "");
        // checking whether the day is in current month or not.  
        if ((Integer.parseInt(gridvalue) > 1) && (position < firstDay)) {
            // setting offdays to white color.
            dayView.setTextColor(Color.RED);
            dayView.setClickable(false);
            dayView.setFocusable(false);
            dayView.setVisibility(View.GONE);
            lout_top.setVisibility(View.GONE);
        } else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
            dayView.setTextColor(Color.RED);
            dayView.setClickable(false);
            dayView.setFocusable(false);
            dayView.setVisibility(View.GONE);
            lout_top.setVisibility(View.GONE);
        } else {
            // setting curent month's days in blue color.
            //dayView.setTextColor(Color.BLUE);
//            dayView.setTextColor(Color.parseColor("#ffffff"));
//            lout_top.setBackgroundResource(R.drawable.date_selected_new);

            if (dayString.get(position).equals(curentDateString)) {
            dayView.setTextColor(Color.parseColor("#ffffff"));
            lout_top.setBackgroundResource(R.drawable.date_selected_new);
             //   setSelected(v);
              //  previousView = v;
            } else {
                  lout_top.setBackgroundResource(R.drawable.date_unselected_new);
                //  v.setBackgroundResource(R.drawable.date_unselected);
                 dayView.setTextColor(Color.parseColor("#333333"));

             //   setSelected(v);
             //   previousView = v;
            }
        }

//        if (dayString.get(position).equals(curentDateString)) {
////            dayView.setTextColor(Color.parseColor("#ffffff"));
////            lout_top.setBackgroundResource(R.drawable.date_selected_new);
//            setSelected(v);
//            previousView = v;
//        } else {
//          //  lout_top.setBackgroundResource(R.drawable.date_unselected_new);
//          //  v.setBackgroundResource(R.drawable.date_unselected);
//           // dayView.setTextColor(Color.parseColor("#333333"));
//        }
        dayView.setText(gridvalue);
       // dayView.setText(gridvalue+"\n"+"RH");

        // create date String for comparison
        String date = dayString.get(position);

        if (date.length() == 1) {
            date = "0" + date;
        }
        String monthStr = "" + (month.get(GregorianCalendar.MONTH) + 1);
        if (monthStr.length() == 1) {
            monthStr = "0" + monthStr;
        }

        // show icon if date is not empty and it exists in the items array
        TextView txtLeaveType = (TextView) v.findViewById(R.id.txtLeaveType);

        if(typeList.size()>0) {
            for (int i = 0; i < typeList.size(); i++) {
                HolidayModel type = typeList.get(i);
                if (Utility.INSTANCE.getDateCompare(type.getDate(), date)) {

                    if (type.getCount() > 0) {
                        txtLeaveType.setText(type.getLeaveAbbreviation());
                        txtLeaveType.setVisibility(View.VISIBLE);

                  //   Need to change after discuss
                     /*   String abbriviation = type.getLeaveAbbreviation();
                        if(abbriviation.contains("/")){
                            String[] stringArray = abbriviation.split("/");
                            if(stringArray[2].equals("CO") || stringArray[2].equals("Sal")){
                                if(type.getRequestStatus().equals("Pending"))
                                    txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
                                else
                                    txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                            }
                            else if(stringArray[0].equals("RH") || stringArray[0].equals("GH")){
                                if(type.getRequestStatus().equals("Pending"))
                                    txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
                                else
                                    txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                            }
                        }
*/
                        if(type.getRequestStatus().equals("")){
                            if(type.getLeaveAbbreviation().equals("RH"))
                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                            else if(type.getLeaveAbbreviation().equals("GH"))
                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                        }
                        else{
                            if(type.getRequestStatus().equals("Pending"))
                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
                            else
                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                        }

//                        if(type.getLeaveAbbreviation().equals("RH"))
//                            txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.red));
//                        else if(type.getLeaveAbbreviation().equals("GH"))
//                            txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
//                        else{
//                            if(type.getRequestStatus().equals("Pending"))
//                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.orange));
//                            else
//                                txtLeaveType.setTextColor(ContextCompat.getColor(mContext, R.color.green));
//                           }
                    }
                }
            }
        }
        return v;
    }

    public View setSelected(View view) {
//        if (previousView != null) {
//            previousView.setBackgroundResource(R.drawable.date_unselected);
//            TextView dayView = (TextView) previousView.findViewById(R.id.date);
//            dayView.setTextColor(Color.parseColor("#333333"));
//            ImageView iw = (ImageView)previousView.findViewById(R.id.date_icon);
//            iw.setImageResource(R.drawable.ic_dots);
//        }
//        previousView = view;
//        view.setBackgroundResource(R.drawable.date_selected);
//        TextView dayView = (TextView) previousView.findViewById(R.id.date);
//        dayView.setTextColor(Color.parseColor("#ffffff"));
//        ImageView iw = (ImageView)previousView.findViewById(R.id.date_icon);
//        iw.setImageResource(R.drawable.ic_dots_white);
        return view;
    }

    public void refreshDays() {
        // clear items  
        items.clear();
        dayString.clear();
        Locale.setDefault(Locale.US);
        pmonth = (GregorianCalendar) month.clone();
        // month start day. ie; sun, mon, etc  
        firstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
        // finding number of weeks in current month.  
        maxWeeknumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        // allocating maximum row number for the gridview.  
        mnthlength = maxWeeknumber * 7;
        maxP = getMaxP(); // previous month maximum day 31,30....
        calMaxP = maxP - (firstDay - 1);// calendar offday starting 24,25 ...  
        /**
         * Calendar instance for getting a complete gridview including the three 
         * month's (previous,current,next) dates. 
         */
        pmonthmaxset = (GregorianCalendar) pmonth.clone();
        /**
         * setting the start date as previous month's required date. 
         */
        pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

        /**
         * filling calendar gridview. 
         */
        for (int n = 0; n < mnthlength; n++) {

            itemvalue = df.format(pmonthmaxset.getTime());
            pmonthmaxset.add(GregorianCalendar.DATE, 1);
            dayString.add(itemvalue);

        }
    }
 
    private int getMaxP() {
        int maxP;
        if (month.get(GregorianCalendar.MONTH) == month
                .getActualMinimum(GregorianCalendar.MONTH)) {
            pmonth.set((month.get(GregorianCalendar.YEAR) - 1),
                    month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            pmonth.set(GregorianCalendar.MONTH,
                    month.get(GregorianCalendar.MONTH) - 1);
        }
        maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

        return maxP;
    }

}  