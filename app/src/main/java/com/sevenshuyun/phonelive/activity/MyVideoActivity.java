package com.sevenshuyun.phonelive.activity;

import android.view.ViewGroup;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.utils.WordUtil;
import com.sevenshuyun.phonelive.views.VideoHomeViewHolder;

/**
 * Created by cxf on 2018/12/14.
 */

public class MyVideoActivity extends AbsActivity {

    private VideoHomeViewHolder mVideoHomeViewHolder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_video;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.video_my_video));
        mVideoHomeViewHolder = new VideoHomeViewHolder(mContext, (ViewGroup) findViewById(R.id.container), AppConfig.getInstance().getUid());
        mVideoHomeViewHolder.addToParent();
        addLifeCycleListener(mVideoHomeViewHolder.getLifeCycleListener());
        mVideoHomeViewHolder.loadData();
    }

    private void release(){
        if(mVideoHomeViewHolder!=null){
            mVideoHomeViewHolder.release();
        }
        mVideoHomeViewHolder=null;
    }

    @Override
    public void onBackPressed() {
        release();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }
}
