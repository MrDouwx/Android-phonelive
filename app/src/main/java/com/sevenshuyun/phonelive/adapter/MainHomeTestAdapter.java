package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sevenshuyun.phonelive.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 */

public class MainHomeTestAdapter extends RecyclerView.Adapter<MainHomeTestAdapter.Vh> {

    private List<String> mList;
    private LayoutInflater mInflater;

    public MainHomeTestAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            mList.add(String.valueOf(i));
        }
    }

    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_test, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {
        vh.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public Vh(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }

        void setData(String data) {
            mTextView.setText(data);
        }
    }
}
