package com.sevenshuyun.phonelive.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.VideoPlayActivity;
import com.sevenshuyun.phonelive.activity.VideoReportActivity;
import com.sevenshuyun.phonelive.adapter.VideoShareAdapter;
import com.sevenshuyun.phonelive.bean.ConfigBean;
import com.sevenshuyun.phonelive.bean.VideoBean;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.mob.MobBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/19.
 * 视频分享弹窗
 */

public class VideoShareDialogFragment extends AbsDialogFragment implements OnItemClickListener<MobBean> {

    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;
    private VideoBean mVideoBean;


    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_share;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mVideoBean = bundle.getParcelable(Constants.VIDEO_BEAN);
        if (mVideoBean == null) {
            return;
        }
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView2 = (RecyclerView) mRootView.findViewById(R.id.recyclerView_2);
        mRecyclerView2.setHasFixedSize(true);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        List<MobBean> list = null;
        ConfigBean configBean = AppConfig.getInstance().getConfig();
        if (configBean != null) {
            list = MobBean.getVideoShareTypeList(configBean.getVideoShareTypes());
        }
        if (list != null) {
            VideoShareAdapter adapter = new VideoShareAdapter(mContext, list);
            adapter.setOnItemClickListener(this);
            mRecyclerView.setAdapter(adapter);
        }
        List<MobBean> list2 = new ArrayList<>();
        MobBean linkBean = new MobBean();
        linkBean.setType(Constants.LINK);
        linkBean.setName(R.string.copy_link);
        linkBean.setIcon1(R.mipmap.icon_share_video_link);
        list2.add(linkBean);
        MobBean reportBean = new MobBean();
        if (mVideoBean.getUid().equals(AppConfig.getInstance().getUid())) {//自己的视频
            reportBean.setType(Constants.DELETE);
            reportBean.setName(R.string.delete);
            reportBean.setIcon1(R.mipmap.icon_share_video_delete);
        } else {
            reportBean.setType(Constants.REPORT);
            reportBean.setName(R.string.report);
            reportBean.setIcon1(R.mipmap.icon_share_video_report);
        }
        list2.add(reportBean);
        MobBean saveBean = new MobBean();
        saveBean.setType(Constants.SAVE);
        saveBean.setName(R.string.save);
        saveBean.setIcon1(R.mipmap.icon_share_video_save);
        list2.add(saveBean);
        VideoShareAdapter adapter2 = new VideoShareAdapter(mContext, list2);
        adapter2.setOnItemClickListener(this);
        mRecyclerView2.setAdapter(adapter2);
    }

    @Override
    public void onItemClick(MobBean bean, int position) {
        if (!canClick()) {
            return;
        }
        dismiss();
        switch (bean.getType()) {
            case Constants.LINK://复制链接
                ((VideoPlayActivity) mContext).copyLink(mVideoBean);
                break;
            case Constants.REPORT://举报
                VideoReportActivity.forward(mContext, mVideoBean.getId());
                break;
            case Constants.SAVE://保存
                ((VideoPlayActivity) mContext).downloadVideo(mVideoBean);
                break;
            case Constants.DELETE://删除
                ((VideoPlayActivity) mContext).deleteVideo(mVideoBean);
                break;
            default:
                ((VideoPlayActivity) mContext).shareVideoPage(bean.getType(), mVideoBean);
                break;
        }
    }

}
