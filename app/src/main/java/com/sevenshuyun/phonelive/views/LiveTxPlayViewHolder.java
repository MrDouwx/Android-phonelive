package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.interfaces.LifeCycleAdapter;
import com.sevenshuyun.phonelive.utils.DpUtil;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.ToastUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

/**
 * Created by cxf on 2018/10/10.
 * 直播间 腾讯播放器
 */

public class LiveTxPlayViewHolder extends LiveRoomPlayViewHolder implements ITXVodPlayListener {

    private static final String TAG = "LiveTxPlayViewHolder";
    private ViewGroup mRoot;
    private ViewGroup mSmallContainer;
    private ViewGroup mLeftContainer;
    private ViewGroup mRightContainer;
    private ViewGroup mPkContainer;
    private TXCloudVideoView mVideoView;
    private View mLoading;
    private ImageView mCover;
    private TXVodPlayer mPlayer;
    private boolean mPaused;//是否切后台了
    private boolean mStarted;//是否开始了播放
    private boolean mEnd;//是否结束了播放
    private boolean mPausedPlay;//是否被动暂停了播放
    private boolean mChangeToLeft;


    public LiveTxPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx;
    }

    @Override
    public void init() {
        mRoot = (ViewGroup) findViewById(R.id.root);
        mSmallContainer = (ViewGroup) findViewById(R.id.small_container);
        mLeftContainer = (ViewGroup) findViewById(R.id.left_container);
        mRightContainer = (ViewGroup) findViewById(R.id.right_container);
        mPkContainer = (ViewGroup) findViewById(R.id.pk_container);
        mLoading = findViewById(R.id.loading);
        mCover = (ImageView) findViewById(R.id.cover);
        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mVideoView.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);

//        mPlayer = new TXLivePlayer(mContext);
//        mPlayer.setPlayerView(mVideoView);
//        mPlayer.setAutoPlay(true);
//        mPlayer.enableHardwareDecode(false);
//        mPlayer.setPlayListener(this);
        mPlayer = new TXVodPlayer(mContext);
        mPlayer.setConfig(new TXVodPlayConfig());
        mPlayer.setPlayerView(mVideoView);
        mPlayer.setVodListener(this);
        mLifeCycleListener = new LifeCycleAdapter() {

            @Override
            public void onResume() {
                if (!mPausedPlay && mPaused && mPlayer != null) {
                    mPlayer.resume();
                }
                mPaused = false;
            }

            @Override
            public void onPause() {
                if (!mPausedPlay && mPlayer != null) {
                    mPlayer.pause();
                }
                mPaused = true;
            }

            @Override
            public void onDestroy() {
                release();
            }
        };
    }


    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
        if (mEnd) {
            return;
        }
        switch (e) {
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://播放开始
                if (mLoading != null && mLoading.getVisibility() == View.VISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
                break;
            case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://第一帧
                hideCover();
                break;
            case TXLiveConstants.PLAY_EVT_PLAY_END://播放结束
                replay();
                break;
            case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION://获取视频宽高
                if (mChangeToLeft) {
                    return;
                }
                float width = bundle.getInt("EVT_PARAM1", 0);
                float height = bundle.getInt("EVT_PARAM2", 0);
                L.e(TAG, "流---width----->" + width);
                L.e(TAG, "流---height----->" + height);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
                int targetH = 0;
                if (width / height > 0.5625f) {//横屏 9:16=0.5625
                    targetH = (int) (mVideoView.getWidth() / width * height);
                } else {
                    targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                if (targetH != params.height) {
                    params.height = targetH;
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    mVideoView.requestLayout();
                }

                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT://播放失败
            case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND:
                ToastUtil.show(WordUtil.getString(R.string.live_play_error));
                break;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }

    @Override
    public void hideCover() {
        if (mCover != null) {
            mCover.animate().alpha(0).setDuration(500).start();
        }
    }

    @Override
    public void setCover(String coverUrl) {
        if (mCover != null) {
            ImgLoader.displayBlur(coverUrl, mCover);
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
        if (mStarted && mPlayer != null) {
            mPlayer.seek(0);
            mPlayer.resume();
        }
    }


    /**
     * 暂停播放
     */
    @Override
    public void pausePlay() {
        if (!mPausedPlay) {
            mPausedPlay = true;
            if (!mPaused) {
                if (mPlayer != null) {
                    mPlayer.pause();
                }
            }
            if (mCover != null) {
                mCover.setAlpha(1f);
                if (mCover.getVisibility() != View.VISIBLE) {
                    mCover.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 暂停播放后恢复
     */
    @Override
    public void resumePlay() {
        if (mPausedPlay) {
            mPausedPlay = false;
            if (!mPaused) {
                if (mPlayer != null) {
                    mPlayer.resume();
                }
            }
            hideCover();
        }
    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.startPlay(url);
        }
        mStarted = true;
        L.e(TAG, "play----url--->" + url);
    }

    @Override
    public void stopPlay() {
        mChangeToLeft = false;
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
        }
        if (mCover != null) {
            mCover.setAlpha(1f);
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void release() {
        mEnd = true;
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        mPlayer = null;
        L.e(TAG, "release------->");
    }


    @Override
    public ViewGroup getSmallContainer() {
        return mSmallContainer;
    }


    @Override
    public ViewGroup getRightContainer() {
        return mRightContainer;
    }

    @Override
    public ViewGroup getPkContainer() {
        return mPkContainer;
    }

    @Override
    public void changeToLeft() {
        mChangeToLeft = true;
        if (mVideoView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mVideoView.getWidth() / 2, DpUtil.dp2px(250));
            params.setMargins(0, DpUtil.dp2px(130), 0, 0);
            mVideoView.setLayoutParams(params);
        }
        if (mLoading != null && mLeftContainer != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.gravity = Gravity.CENTER;
            mLoading.setLayoutParams(params);
            mLeftContainer.addView(mLoading);
        }
    }

    @Override
    public void changeToBig() {
        mChangeToLeft = false;
        if (mVideoView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoView.setLayoutParams(params);
        }
        if (mLoading != null && mRoot != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mLoading.setLayoutParams(params);
            mRoot.addView(mLoading);
        }
    }


}
