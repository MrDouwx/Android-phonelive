package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.GuardAdapter;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.bean.GuardUserBean;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/15.
 */

public class LiveGuardListActivity extends AbsActivity {

    public static void forward(Context context, String toUid) {
        Intent intent = new Intent(context, LiveGuardListActivity.class);
        intent.putExtra(Constants.TO_UID, toUid);
        context.startActivity(intent);
    }

    private RefreshView mRefreshView;
    private GuardAdapter mGuardAdapter;
    private String mToUid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_guard_list;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.guard_list));
        mToUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(mToUid)) {
            return;
        }
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        boolean self = mToUid.equals(AppConfig.getInstance().getUid());
        mRefreshView.setNoDataLayoutId(self ? R.layout.view_no_data_guard_anc_2 : R.layout.view_no_data_guard_aud_2);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<GuardUserBean>() {
            @Override
            public RefreshAdapter<GuardUserBean> getAdapter() {
                if (mGuardAdapter == null) {
                    mGuardAdapter = new GuardAdapter(mContext, false);
                }
                return mGuardAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getGuardList(mToUid, p, callback);
            }

            @Override
            public List<GuardUserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), GuardUserBean.class);
            }

            @Override
            public void onRefresh(List<GuardUserBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.GET_GUARD_LIST);
        super.onDestroy();
    }
}
