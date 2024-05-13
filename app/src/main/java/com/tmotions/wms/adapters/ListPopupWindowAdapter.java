package com.tmotions.wms.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tmotions.wms.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phanvanlinh on 27/04/2017.
 * Email: phanvanlinh.94vn@gmail.com
 */

public class ListPopupWindowAdapter extends BaseAdapter{
    private Activity mActivity;
    private List<String> mDataSource = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private OnClickDeleteButtonListener clickDeleteButtonListener;

    public ListPopupWindowAdapter(Activity activity, List<String> dataSource){
        this.mActivity = activity;
        this.mDataSource = dataSource;
        layoutInflater = mActivity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public String getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.dropdown_item2, null);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        // bind data
        holder.tvTitle.setText(getItem(position));

        return convertView;
    }

    public class ViewHolder{
        private TextView tvTitle;
    }

    // interface to return callback to activity
    public interface OnClickDeleteButtonListener{
        void onClickDeleteButton(int position);
    }
}
