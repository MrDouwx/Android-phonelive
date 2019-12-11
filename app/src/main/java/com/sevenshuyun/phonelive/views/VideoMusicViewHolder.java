package com.sevenshuyun.phonelive.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.MusicAdapter;
import com.sevenshuyun.phonelive.adapter.MusicClassAdapter;
import com.sevenshuyun.phonelive.bean.MusicBean;
import com.sevenshuyun.phonelive.bean.MusicClassBean;
import com.sevenshuyun.phonelive.dialog.VideoMusicClassDialog;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.LifeCycleAdapter;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.interfaces.VideoMusicActionListener;
import com.sevenshuyun.phonelive.utils.DialogUitl;
import com.sevenshuyun.phonelive.utils.DownloadUtil;
import com.sevenshuyun.phonelive.utils.MusicMediaPlayerUtil;
import com.sevenshuyun.phonelive.utils.ScreenDimenUtil;
import com.sevenshuyun.phonelive.utils.ToastUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/7.
 * 视频音乐逻辑
 */

public class VideoMusicViewHolder extends AbsViewHolder implements View.OnClickListener, OnItemClickListener<MusicClassBean>, VideoMusicActionListener {

    private View mRoot;
    private RecyclerView mMusicClassRecyclerView;//音乐分类recyclerView
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private boolean mAnimating;
    private boolean mShowed;
    private ViewGroup mContainer1;
    private VideoMusicHotViewHolder mHotViewHolder;
    private VideoMusicCollectViewHolder mCollectViewHolder;
    private List<VideoMusicChildViewHolder> mVideoMusicChildViewHolderList;
    private boolean mLoadData;
    private MusicMediaPlayerUtil mMusicMediaPlayerUtil;
    private DownloadUtil mDownloadUtil;
    private VideoMusicSearchViewHolder mSearchViewHolder;
    private EditText mInput;
    private MyHandler mHandler;
    private ActionListener mActionListener;

    public VideoMusicViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_music;
    }

    @Override
    public void init() {
        mRoot = findViewById(R.id.root);
        View group = findViewById(R.id.group);
        int screenHeight = ScreenDimenUtil.getInstance().getScreenHeight();
        group.setTranslationY(screenHeight);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator = ObjectAnimator.ofFloat(group, "translationY", 0);
        mShowAnimator.setInterpolator(interpolator);
        mShowAnimator.setDuration(300);
        mHideAnimator = ObjectAnimator.ofFloat(group, "translationY", screenHeight);
        mHideAnimator.setInterpolator(interpolator);
        mHideAnimator.setDuration(300);
        mShowAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
                if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
                    mRoot.setVisibility(View.VISIBLE);
                }
                mShowed = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                loadData();
            }
        });
        mHideAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
                if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                    mRoot.setVisibility(View.INVISIBLE);
                }
                if (mActionListener != null) {
                    mActionListener.onHide();
                }
            }

        });
        mMusicClassRecyclerView = (RecyclerView) findViewById(R.id.music_class_recyclerView);
        mMusicClassRecyclerView.setHasFixedSize(true);
        mMusicClassRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mContainer1 = (ViewGroup) findViewById(R.id.container_1);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_hot).setOnClickListener(this);
        findViewById(R.id.btn_favorite).setOnClickListener(this);
        mHotViewHolder = new VideoMusicHotViewHolder(mContext, mContainer1, this);
        mHotViewHolder.addToParent();
        mLifeCycleListener = new LifeCycleAdapter() {
            @Override
            public void onResume() {
                if (mMusicMediaPlayerUtil != null) {
                    mMusicMediaPlayerUtil.resumePlay();
                }
            }

            @Override
            public void onPause() {
                if (mMusicMediaPlayerUtil != null) {
                    mMusicMediaPlayerUtil.pausePlay();
                }
            }
        };
        mVideoMusicChildViewHolderList = new ArrayList<>();
        mVideoMusicChildViewHolderList.add(mHotViewHolder);
        mHandler = new MyHandler(this);
        mInput = (EditText) findViewById(R.id.input);
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchMusic();
                    return true;
                }
                return false;
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HttpUtil.cancel(HttpConsts.VIDEO_SEARCH_MUSIC);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (!TextUtils.isEmpty(s)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                } else {
                    if (mSearchViewHolder != null) {
                        mSearchViewHolder.clearData();
                        mSearchViewHolder.hide();
                        onStopMusic();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void loadData() {
        if (mLoadData) {
            return;
        }
        mLoadData = true;
        HttpUtil.getMusicClassList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0 && mMusicClassRecyclerView != null) {
                    List<MusicClassBean> list = JSON.parseArray(Arrays.toString(info), MusicClassBean.class);
                    MusicClassAdapter adapter = new MusicClassAdapter(mContext, list);
                    adapter.setOnItemClickListener(VideoMusicViewHolder.this);
                    mMusicClassRecyclerView.setAdapter(adapter);
                }
            }
        });
        if (mHotViewHolder != null) {
            mHotViewHolder.loadData();
        }
    }

    /**
     * 搜索音乐
     */
    private void searchMusic() {
        if (mInput == null) {
            return;
        }
        String key = mInput.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            ToastUtil.show(R.string.content_empty);
            return;
        }
        HttpUtil.cancel(HttpConsts.VIDEO_SEARCH_MUSIC);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mSearchViewHolder == null) {
            mSearchViewHolder = new VideoMusicSearchViewHolder(mContext, (ViewGroup) findViewById(R.id.container_2), this);
            mSearchViewHolder.addToParent();
            if (mVideoMusicChildViewHolderList != null) {
                mVideoMusicChildViewHolderList.add(mSearchViewHolder);
            }
        }
        doStopMusic();
        mSearchViewHolder.show();
        mSearchViewHolder.setKey(key);
        mSearchViewHolder.loadData();
    }

    public void show() {
        if (!mShowed && mShowAnimator != null) {
            mShowAnimator.start();
        }
    }

    public void hide() {
        doStopMusic();
        if (mShowed && mHideAnimator != null) {
            mHideAnimator.start();
        }
    }

    public boolean isShowed() {
        return mShowed;
    }

    public void release() {
        mActionListener = null;
        if (mMusicMediaPlayerUtil != null) {
            mMusicMediaPlayerUtil.destroy();
        }
        mMusicMediaPlayerUtil = null;
        HttpUtil.cancel(HttpConsts.GET_MUSIC_CLASS_LIST);
        HttpUtil.cancel(HttpConsts.GET_HOT_MUSIC_LIST);
        HttpUtil.cancel(HttpConsts.GET_MUSIC_COLLECT_LIST);
        HttpUtil.cancel(HttpConsts.SET_MUSIC_COLLECT);
        HttpUtil.cancel(HttpConsts.VIDEO_SEARCH_MUSIC);
        if (mHandler != null) {
            mHandler.release();
        }
        mHandler = null;
    }

    @Override
    public void onClick(View v) {
        if (mAnimating) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_close:
                hide();
                break;
            case R.id.btn_hot://热门歌曲
                hideCollect();
                break;
            case R.id.btn_favorite://我的收藏
                showCollect();
                break;
        }
    }

    private void showCollect() {
        doStopMusic();
        if (mCollectViewHolder == null) {
            mCollectViewHolder = new VideoMusicCollectViewHolder(mContext, mContainer1, this);
            mCollectViewHolder.addToParent();
            if (mVideoMusicChildViewHolderList != null) {
                mVideoMusicChildViewHolderList.add(mCollectViewHolder);
            }
        }
        mCollectViewHolder.show();
        mCollectViewHolder.loadData();
    }

    private void hideCollect() {
        doStopMusic();
        if (mCollectViewHolder != null) {
            mCollectViewHolder.hide();
        }
    }


    /**
     * 点击音乐分类回调
     */
    @Override
    public void onItemClick(MusicClassBean bean, int position) {
        if (!canClick()) {
            return;
        }
        doStopMusic();
        VideoMusicClassDialog dialog = new VideoMusicClassDialog(mContext, mParentView, bean.getTitle(), bean.getId(), this);
        dialog.show();
    }

    /**
     * 播放音乐回调
     */
    @Override
    public void onPlayMusic(final MusicAdapter adapter, final MusicBean bean, final int position) {
        String fileName = Constants.VIDEO_MUSIC_NAME_PREFIX + bean.getId();
        String path = AppConfig.MUSIC_PATH + fileName;
        File file = new File(path);
        if (file.exists()) {
            bean.setLocalPath(path);
            adapter.expand(position);
            startPlayMusic(path);
        } else {
            final Dialog dialog = DialogUitl.loadingDialog(mContext);
            dialog.show();
            if (mDownloadUtil == null) {
                mDownloadUtil = new DownloadUtil();
            }
            mDownloadUtil.download(fileName, AppConfig.MUSIC_PATH, fileName, bean.getFileUrl(), new DownloadUtil.Callback() {
                @Override
                public void onSuccess(File file) {
                    dialog.dismiss();
                    String musicPath = file.getAbsolutePath();
                    bean.setLocalPath(musicPath);
                    adapter.expand(position);
                    startPlayMusic(musicPath);
                }

                @Override
                public void onProgress(int progress) {
                }

                @Override
                public void onError(Throwable e) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void startPlayMusic(String path) {
        if (mMusicMediaPlayerUtil == null) {
            mMusicMediaPlayerUtil = new MusicMediaPlayerUtil();
        }
        mMusicMediaPlayerUtil.startPlay(path);
    }

    /**
     * 停止播放音乐回调
     */
    @Override
    public void onStopMusic() {
        if (mMusicMediaPlayerUtil != null) {
            mMusicMediaPlayerUtil.stopPlay();
        }
    }

    /**
     * 使用音乐回调
     */
    @Override
    public void onUseClick(MusicBean bean) {
        if (mShowed && mHideAnimator != null) {
            mHideAnimator.start();
        }
        if (mActionListener != null && bean != null) {
            mActionListener.onChooseMusic(bean);
        }
    }

    /**
     * 收藏音乐回调
     */
    @Override
    public void onCollect(MusicAdapter adapter, int musicId, int collect) {
        if (mVideoMusicChildViewHolderList != null) {
            for (VideoMusicChildViewHolder vmcvh : mVideoMusicChildViewHolderList) {
                if (vmcvh != null) {
                    vmcvh.collectChanged(adapter, musicId, collect);
                }
            }
        }
    }

    private void doStopMusic() {
        if (mVideoMusicChildViewHolderList != null) {
            for (VideoMusicChildViewHolder vmcvh : mVideoMusicChildViewHolderList) {
                if (vmcvh != null) {
                    vmcvh.collapse();
                }
            }
        }
        onStopMusic();
    }

    private static class MyHandler extends Handler {

        private VideoMusicViewHolder mViewHolder;

        public MyHandler(VideoMusicViewHolder viewHolder) {
            mViewHolder = new WeakReference<>(viewHolder).get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mViewHolder != null) {
                mViewHolder.searchMusic();
            }
        }

        public void release() {
            removeCallbacksAndMessages(null);
            mViewHolder = null;
        }
    }

    public static abstract class ActionListener {
        public abstract void onChooseMusic(MusicBean bean);

        public void onHide() {

        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
