package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.RecommendAdapter;
import com.sevenshuyun.phonelive.bean.RecommendBean;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/11/2.
 */

public class RecommendActivity extends AbsActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private RecommendAdapter mAdapter;
    private boolean mShowInvite;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recommend;
    }

    @Override
    protected void main() {
        mShowInvite = getIntent().getBooleanExtra(Constants.SHOW_INVITE, false);
        findViewById(R.id.btn_enter).setOnClickListener(this);
        findViewById(R.id.btn_skip).setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        HttpUtil.getRecommend(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<RecommendBean> list = JSON.parseArray(Arrays.toString(info), RecommendBean.class);
                    if (mAdapter == null) {
                        mAdapter = new RecommendAdapter(mContext, list);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }
            }
        });
    }

    public static void forward(Context context, boolean showInvite) {
        Intent intent = new Intent(context, RecommendActivity.class);
        intent.putExtra(Constants.SHOW_INVITE, showInvite);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_enter:
                enter();
                break;
            case R.id.btn_skip:
                skip();
                break;
        }
    }

    private void enter() {
        if (mAdapter == null) {
            skip();
            return;
        }
        String uids = mAdapter.getCheckedUid();
        if (TextUtils.isEmpty(uids)) {
            skip();
            return;
        }
        HttpUtil.recommendFollow(uids, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    skip();
                }
            }
        });
    }

    /**
     * 跳过
     */
    private void skip() {
        MainActivity.forward(mContext, mShowInvite);
        finish();
    }

    @Override
    public void onBackPressed() {
        skip();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpConsts.GET_RECOMMEND);
        HttpUtil.cancel(HttpConsts.RECOMMEND_FOLLOW);
        super.onDestroy();
    }
}
