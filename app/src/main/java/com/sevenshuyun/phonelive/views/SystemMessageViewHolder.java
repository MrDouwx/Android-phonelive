package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.adapter.SystemMessageAdapter;
import com.sevenshuyun.phonelive.bean.SystemMessageBean;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/28.
 */

public class SystemMessageViewHolder extends AbsViewHolder implements View.OnClickListener {

    private RefreshView mRefreshView;
    private SystemMessageAdapter mAdapter;
    private ActionListener mActionListener;


    public SystemMessageViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_sys_msg;
    }

    @Override
    public void init() {
        findViewById(R.id.btn_back).setOnClickListener(this);
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_sys_msg);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<SystemMessageBean>() {
            @Override
            public RefreshAdapter<SystemMessageBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new SystemMessageAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getSystemMessageList(p, callback);
            }

            @Override
            public List<SystemMessageBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), SystemMessageBean.class);
            }

            @Override
            public void onRefresh(List<SystemMessageBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
    }

    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    public void release() {
        mActionListener=null;
        HttpUtil.cancel(HttpConsts.GET_SYSTEM_MESSAGE_LIST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if(mActionListener!=null){
                    mActionListener.onBackClick();
                }
                break;
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener{
        void onBackClick();
    }
}
