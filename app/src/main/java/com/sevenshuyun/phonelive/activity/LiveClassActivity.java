package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.MainHomeHotAdapter;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.bean.LiveBean;
import com.sevenshuyun.phonelive.custom.ItemDecoration;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.presenter.CheckLivePresenter;
import com.sevenshuyun.phonelive.utils.LiveStorge;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/10/27.
 */

public class LiveClassActivity extends AbsActivity implements OnItemClickListener<LiveBean> {

    private int mClassId;
    private RefreshView mRefreshView;
    private MainHomeHotAdapter mAdapter;
    private CheckLivePresenter mCheckLivePresenter;

    public static void forward(Context context, int classId, String className) {
        Intent intent = new Intent(context, LiveClassActivity.class);
        intent.putExtra(Constants.CLASS_ID, classId);
        intent.putExtra(Constants.CLASS_NAME, className);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_class;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mClassId = intent.getIntExtra(Constants.CLASS_ID, -1);
        if (mClassId < 0) {
            return;
        }
        String liveClassName = intent.getStringExtra(Constants.CLASS_NAME);
        setTitle(liveClassName);
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_live_class);
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 5);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new RefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeHotAdapter(mContext);
                    mAdapter.setOnItemClickListener(LiveClassActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                HttpUtil.getClassLive(mClassId, p, callback);
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), LiveBean.class);
            }

            @Override
            public void onRefresh(List<LiveBean> list) {
                LiveStorge.getInstance().put(Constants.LIVE_CLASS_PREFIX + mClassId, list);
            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {
//                if (dataCount < 10) {
//                    mRefreshView.setLoadMoreEnable(false);
//                } else {
//                    mRefreshView.setLoadMoreEnable(true);
//                }
            }
        });
    }

    @Override
    public void onItemClick(LiveBean bean, int position) {
        watchLive(bean, position);
    }


    /**
     * 观看直播
     */
    public void watchLive(LiveBean liveBean, int position) {
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new CheckLivePresenter(mContext);
        }
        mCheckLivePresenter.watchLive(liveBean, Constants.LIVE_CLASS_PREFIX + mClassId, position);
    }

    @Override
    protected void onDestroy() {
        LiveStorge.getInstance().remove(Constants.LIVE_CLASS_PREFIX + mClassId);
        HttpUtil.cancel(HttpConsts.GET_CLASS_LIVE);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshView.initData();
    }

}
