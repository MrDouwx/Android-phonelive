package com.sevenshuyun.phonelive.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.LevelBean;
import com.sevenshuyun.phonelive.bean.SearchUserBean;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.dialog.LiveShareDialogFragment;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;
import com.sevenshuyun.phonelive.presenter.UserHomeSharePresenter;
import com.sevenshuyun.phonelive.utils.StringUtil;
import com.sevenshuyun.phonelive.views.LiveRecordPlayViewHolder;

/**
 * Created by cxf on 2018/10/29.
 */

public class LiveRecordPlayActivity extends AbsActivity implements
        LiveRecordPlayViewHolder.ActionListener, View.OnClickListener, LiveShareDialogFragment.ActionListener {

    private LiveRecordPlayViewHolder mLiveRecordPlayViewHolder;
    private ImageView mAvatar;
    private ImageView mLevelAnchor;
    private TextView mName;
    private TextView mID;
    private View mBtnFollow;
    private SeekBar mSeekBar;
    private long mDuration;
    private TextView mCurTimeTextView;
    private TextView mDurationTextView;
    private ImageView mBtnPlay;
    private UserBean mUserBean;
    private UserHomeSharePresenter mUserHomeSharePresenter;
    private boolean mPausePlay;

    public static void forward(Context context, String url, UserBean userBean) {
        if (TextUtils.isEmpty(url) || userBean == null) {
            return;
        }
        Intent intent = new Intent(context, LiveRecordPlayActivity.class);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.USER_BEAN, userBean);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_record_play;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mUserBean = intent.getParcelableExtra(Constants.USER_BEAN);
        if (mUserBean == null) {
            return;
        }
        String url = intent.getStringExtra(Constants.URL);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mName = (TextView) findViewById(R.id.name);
        mID = (TextView) findViewById(R.id.id_val);
        mBtnFollow = findViewById(R.id.btn_follow);
        ImgLoader.displayAvatar(mUserBean.getAvatar(), mAvatar);
        LevelBean levelBean = AppConfig.getInstance().getAnchorLevel(mUserBean.getLevelAnchor());
        if (levelBean != null) {
            ImgLoader.display(levelBean.getThumbIcon(), mLevelAnchor);
        }
        mName.setText(mUserBean.getUserNiceName());
        mID.setText(mUserBean.getLiangNameTip());
        if (mUserBean instanceof SearchUserBean) {
            SearchUserBean searchUserBean = (SearchUserBean) mUserBean;
            int attention = searchUserBean.getAttention();
            if (attention == 0) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
            }
            mBtnFollow.setOnClickListener(this);
        }
        mUserHomeSharePresenter = new UserHomeSharePresenter(mContext);
        mUserHomeSharePresenter.setToUid(mUserBean.getId())
                .setToName(mUserBean.getUserNiceName())
                .setAvatarThumb(mUserBean.getAvatarThumb())
                .setFansNum(String.valueOf(mUserBean.getFans()));
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mLiveRecordPlayViewHolder != null) {
                    mLiveRecordPlayViewHolder.clickResume();
                    int progress = seekBar.getProgress();
                    mLiveRecordPlayViewHolder.seekTo(mDuration * progress / 100000f);
                }
            }
        });
        mCurTimeTextView = findViewById(R.id.cur_time);
        mDurationTextView = findViewById(R.id.duration);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        mLiveRecordPlayViewHolder = new LiveRecordPlayViewHolder(mContext, container);
        mLiveRecordPlayViewHolder.setActionListener(this);
        addLifeCycleListener(mLiveRecordPlayViewHolder.getLifeCycleListener());
        mLiveRecordPlayViewHolder.addToParent();
        mLiveRecordPlayViewHolder.play(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mLiveRecordPlayViewHolder != null) {
            mLiveRecordPlayViewHolder.release();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLiveRecordPlayViewHolder != null) {
            mLiveRecordPlayViewHolder.release();
        }
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.release();
        }
        super.onDestroy();
    }

    @Override
    public void onProgress(int progress) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
        }
    }

    @Override
    public void onDuration(long duration) {
        mDuration = duration;
        if (mDurationTextView != null) {
            mDurationTextView.setText(StringUtil.getDurationText(duration));
        }
    }

    @Override
    public void onCurTime(long curTime) {
        if (mCurTimeTextView != null) {
            mCurTimeTextView.setText(StringUtil.getDurationText(curTime));
        }
    }

    @Override
    public void onClickPause() {
        if (mBtnPlay != null) {
            mBtnPlay.setImageResource(R.mipmap.icon_live_record_play);
        }
    }

    @Override
    public void onClickResume() {
        if (mBtnPlay != null) {
            mBtnPlay.setImageResource(R.mipmap.icon_live_record_pause);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_share:
                share();
                break;
            case R.id.btn_play:
                togglePlay();
                break;
            case R.id.btn_follow:
                follow();
                break;
        }
    }

    /**
     * 切换播放暂停
     */
    private void togglePlay() {
        if (mLiveRecordPlayViewHolder != null) {
            if (mPausePlay) {
                mLiveRecordPlayViewHolder.clickResume();
            } else {
                mLiveRecordPlayViewHolder.clickPause();
            }
            mPausePlay = !mPausePlay;
        }
    }


    /**
     * 关注主播
     */
    private void follow() {
        if (mUserBean == null) {
            return;
        }
        HttpUtil.setAttention(Constants.FOLLOW_FROM_LIVE, mUserBean.getId(), new CommonCallback<Integer>() {
            @Override
            public void callback(Integer isAttention) {
                if (isAttention == 1) {
                    if (mBtnFollow.getVisibility() == View.VISIBLE) {
                        mBtnFollow.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * 分享
     */
    private void share() {
        LiveShareDialogFragment fragment = new LiveShareDialogFragment();
        fragment.setActionListener(this);
        fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveShareDialogFragment");
    }

    @Override
    public void onItemClick(String type) {
        if (Constants.LINK.equals(type)) {
            copyLink();
        } else {
            shareHomePage(type);
        }
    }

    /**
     * 复制页面链接
     */
    private void copyLink() {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.copyLink();
        }
    }


    /**
     * 分享页面链接
     */
    private void shareHomePage(String type) {
        if (mUserHomeSharePresenter != null) {
            mUserHomeSharePresenter.shareHomePage(type);
        }
    }

}
