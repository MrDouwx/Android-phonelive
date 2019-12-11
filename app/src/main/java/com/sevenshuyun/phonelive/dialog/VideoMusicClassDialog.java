package com.sevenshuyun.phonelive.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.MusicAdapter;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.bean.MusicBean;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.VideoMusicActionListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/7.
 */

public class VideoMusicClassDialog extends PopupWindow implements View.OnClickListener {

    private Context mContext;
    private View mParent;
    private String mTitle;
    private String mClassId;
    private RefreshView mRefreshView;
    private MusicAdapter mAdapter;
    private VideoMusicActionListener mActionListener;

    public VideoMusicClassDialog(Context context, View parent, String title, String classId, VideoMusicActionListener actionListener) {
        mContext = context;
        mParent = parent;
        mTitle = title;
        mClassId = classId;
        mActionListener = actionListener;
        setContentView(initView());
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(true);
        setClippingEnabled(false);
        setFocusable(true);
        setAnimationStyle(R.style.leftToRightAnim);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                HttpUtil.cancel(HttpConsts.GET_MUSIC_LIST);
                if (mAdapter != null) {
                    mAdapter.setActionListener(null);
                }
                if (mActionListener != null) {
                    mActionListener.onStopMusic();
                }
                mActionListener = null;
            }
        });
    }


    private View initView() {
        View v = LayoutInflater.from(mContext).inflate(R.layout.view_video_music_class, null);
        TextView title = v.findViewById(R.id.title);
        if (!TextUtils.isEmpty(mTitle)) {
            title.setText(mTitle);
        }
        mRefreshView = v.findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_music_class);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new RefreshView.DataHelper<MusicBean>() {
            @Override
            public RefreshAdapter<MusicBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MusicAdapter(mContext);
                    mAdapter.setActionListener(mActionListener);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mClassId)) {
                    HttpUtil.getMusicList(mClassId, p, callback);
                }
            }

            @Override
            public List<MusicBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
            }

            @Override
            public void onRefresh(List<MusicBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
        v.findViewById(R.id.btn_close).setOnClickListener(this);
        return v;
    }

    public void show() {
        showAtLocation(mParent, Gravity.BOTTOM, 0, 0);
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

}
