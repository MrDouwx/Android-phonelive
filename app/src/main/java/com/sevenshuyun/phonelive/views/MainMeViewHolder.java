package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.EditProfileActivity;
import com.sevenshuyun.phonelive.activity.FansActivity;
import com.sevenshuyun.phonelive.activity.FollowActivity;
import com.sevenshuyun.phonelive.activity.LiveRecordActivity;
import com.sevenshuyun.phonelive.activity.MyCoinActivity;
import com.sevenshuyun.phonelive.activity.MyProfitActivity;
import com.sevenshuyun.phonelive.activity.MyVideoActivity;
import com.sevenshuyun.phonelive.activity.SettingActivity;
import com.sevenshuyun.phonelive.activity.WebViewActivity;
import com.sevenshuyun.phonelive.adapter.MainMeAdapter;
import com.sevenshuyun.phonelive.bean.LevelBean;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.bean.UserItemBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;
import com.sevenshuyun.phonelive.interfaces.LifeCycleAdapter;
import com.sevenshuyun.phonelive.interfaces.MainAppBarLayoutListener;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.utils.IconUtil;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.StringUtil;

import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 我的
 */

public class MainMeViewHolder extends AbsMainChildViewHolder implements OnItemClickListener<UserItemBean>, View.OnClickListener {

    private TextView mTtileView;
    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private ImageView mLevelAnchor;
    private ImageView mLevel;
    private TextView mID;
    private TextView mLive;
    private TextView mFollow;
    private TextView mFans;
    private boolean mPaused;
    private RecyclerView mRecyclerView;
    private MainMeAdapter mAdapter;

    public MainMeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_me;
    }

    @Override
    public void init() {
        super.init();
        mTtileView = (TextView) findViewById(R.id.titleView);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mSex = (ImageView) findViewById(R.id.sex);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mLevel = (ImageView) findViewById(R.id.level);
        mID = (TextView) findViewById(R.id.id_val);
        mLive = (TextView) findViewById(R.id.live);
        mFollow = (TextView) findViewById(R.id.follow);
        mFans = (TextView) findViewById(R.id.fans);
        findViewById(R.id.btn_edit).setOnClickListener(this);
        findViewById(R.id.btn_live).setOnClickListener(this);
        findViewById(R.id.btn_follow).setOnClickListener(this);
        findViewById(R.id.btn_fans).setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mLifeCycleListener = new LifeCycleAdapter() {

            @Override
            public void onResume() {
                if (mPaused && mShowed) {
                    loadData();
                }
                mPaused = false;
            }

            @Override
            public void onPause() {
                mPaused = true;
            }

            @Override
            public void onDestroy() {
                L.e("main----MainMeViewHolder-------LifeCycle---->onDestroy");
                HttpUtil.cancel(HttpConsts.GET_BASE_INFO);
            }
        };
        mAppBarLayoutListener = new MainAppBarLayoutListener() {
            @Override
            public void onOffsetChanged(float rate) {
                mTtileView.setAlpha(rate);
            }
        };
        mNeedDispatch = true;
    }

    @Override
    public void setAppBarLayoutListener(MainAppBarLayoutListener appBarLayoutListener) {
    }


    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            AppConfig appConfig = AppConfig.getInstance();
            UserBean u = appConfig.getUserBean();
            List<UserItemBean> list = appConfig.getUserItemList();
            if (u != null && list != null) {
                showData(u, list);
            }
        }
        HttpUtil.getBaseInfo(mCallback);
    }

    private CommonCallback<UserBean> mCallback = new CommonCallback<UserBean>() {
        @Override
        public void callback(UserBean bean) {
            List<UserItemBean> list = AppConfig.getInstance().getUserItemList();
            if (bean != null) {
                showData(bean, list);
            }
        }
    };

    private void showData(UserBean u, List<UserItemBean> list) {
        ImgLoader.displayAvatar(u.getAvatar(), mAvatar);
        mTtileView.setText(u.getUserNiceName());
        mName.setText(u.getUserNiceName());
        mSex.setImageResource(IconUtil.getSexIcon(u.getSex()));
        AppConfig appConfig = AppConfig.getInstance();
        LevelBean anchorLevelBean = appConfig.getAnchorLevel(u.getLevelAnchor());
        if (anchorLevelBean != null) {
            ImgLoader.display(anchorLevelBean.getThumb(), mLevelAnchor);
        }
        LevelBean levelBean = appConfig.getLevel(u.getLevel());
        if (levelBean != null) {
            ImgLoader.display(levelBean.getThumb(), mLevel);
        }
        mID.setText(u.getLiangNameTip());
        mLive.setText(StringUtil.toWan(u.getLives()));
        mFollow.setText(StringUtil.toWan(u.getFollows()));
        mFans.setText(StringUtil.toWan(u.getFans()));
        if (list != null && list.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new MainMeAdapter(mContext, list);
                mAdapter.setOnItemClickListener(this);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setList(list);
            }
        }
    }

    @Override
    public void onItemClick(UserItemBean bean, int position) {
        String url = bean.getHref();
        if (TextUtils.isEmpty(url)) {
            switch (bean.getId()) {
                case 1:
                    forwardProfit();
                    break;
                case 2:
                    forwardCoin();
                    break;
                case 13:
                    forwardSetting();
                    break;
                case 19:
                    forwardMyVideo();
                    break;

            }
        } else {
            WebViewActivity.forward(mContext, url);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                forwardEditProfile();
                break;
            case R.id.btn_live:
                forwardLiveRecord();
                break;
            case R.id.btn_follow:
                forwardFollow();
                break;
            case R.id.btn_fans:
                forwardFans();
                break;
        }
    }

    /**
     * 编辑个人资料
     */
    private void forwardEditProfile() {
        mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
    }

    /**
     * 我的关注
     */
    private void forwardFollow() {
        Intent intent = new Intent(mContext, FollowActivity.class);
        intent.putExtra(Constants.TO_UID, AppConfig.getInstance().getUid());
        mContext.startActivity(intent);
    }

    /**
     * 我的粉丝
     */
    private void forwardFans() {
        Intent intent = new Intent(mContext, FansActivity.class);
        intent.putExtra(Constants.TO_UID, AppConfig.getInstance().getUid());
        mContext.startActivity(intent);
    }

    /**
     * 直播记录
     */
    private void forwardLiveRecord() {
        LiveRecordActivity.forward(mContext, AppConfig.getInstance().getUserBean());
    }

    /**
     * 我的收益
     */
    private void forwardProfit() {
        mContext.startActivity(new Intent(mContext, MyProfitActivity.class));
    }

    /**
     * 我的钻石
     */
    private void forwardCoin() {
        mContext.startActivity(new Intent(mContext, MyCoinActivity.class));
    }

    /**
     * 设置
     */
    private void forwardSetting() {
        mContext.startActivity(new Intent(mContext, SettingActivity.class));
    }

    /**
     * 我的视频
     */
    private void forwardMyVideo() {
        mContext.startActivity(new Intent(mContext, MyVideoActivity.class));
    }
}
