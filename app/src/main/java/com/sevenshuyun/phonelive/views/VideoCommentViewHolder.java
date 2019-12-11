package com.sevenshuyun.phonelive.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.VideoPlayActivity;
import com.sevenshuyun.phonelive.adapter.RefreshAdapter;
import com.sevenshuyun.phonelive.adapter.VideoCommentAdapter;
import com.sevenshuyun.phonelive.bean.VideoBean;
import com.sevenshuyun.phonelive.bean.VideoCommentBean;
import com.sevenshuyun.phonelive.custom.MyLinearLayout3;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.event.VideoCommentEvent;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/12/3.
 * 视频评论相关
 */

public class VideoCommentViewHolder extends AbsViewHolder implements View.OnClickListener, OnItemClickListener<VideoCommentBean> {

    private View mRoot;
    private MyLinearLayout3 mBottom;
    private RefreshView mRefreshView;
    private TextView mCommentNum;
    private VideoCommentAdapter mVideoCommentAdapter;
    private VideoBean mVideoBean;
    private String mCommentString;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private boolean mAnimating;

    public VideoCommentViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_comment;
    }

    @Override
    public void init() {
        mRoot = findViewById(R.id.root);
        mBottom = (MyLinearLayout3) findViewById(R.id.bottom);
        int height = mBottom.getHeight2();
        mBottom.setTranslationY(height);
        mShowAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", 0);
        mHideAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", height);
        mShowAnimator.setDuration(200);
        mHideAnimator.setDuration(200);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator.setInterpolator(interpolator);
        mHideAnimator.setInterpolator(interpolator);
        AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (animation == mHideAnimator) {
                    if (mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
                        mRoot.setVisibility(View.INVISIBLE);
                    }
                } else if (animation == mShowAnimator) {
                    if (mRefreshView != null) {
                        mRefreshView.initData();
                    }
                }
            }
        };
        mShowAnimator.addListener(animatorListener);
        mHideAnimator.addListener(animatorListener);

        findViewById(R.id.root).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.input).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        mCommentString = WordUtil.getString(R.string.video_comment);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mRefreshView = (RefreshView) findViewById(R.id.refreshView);
        mRefreshView.setNoDataLayoutId(R.layout.view_no_data_comment);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    L.e("onLayoutChildren------>" + e.getMessage());
                }
            }
        });
        mRefreshView.setDataHelper(new RefreshView.DataHelper<VideoCommentBean>() {
            @Override
            public RefreshAdapter<VideoCommentBean> getAdapter() {
                if (mVideoCommentAdapter == null) {
                    mVideoCommentAdapter = new VideoCommentAdapter(mContext);
                    mVideoCommentAdapter.setOnItemClickListener(VideoCommentViewHolder.this);
                }
                return mVideoCommentAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (mVideoBean != null) {
                    HttpUtil.getVideoCommentList(mVideoBean.getId(), p, callback);
                }
            }

            @Override
            public List<VideoCommentBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                String commentNum = obj.getString("comments");
                if (mVideoBean != null) {
                    EventBus.getDefault().post(new VideoCommentEvent(mVideoBean.getId(), commentNum));
                }
                if (mCommentNum != null) {
                    mCommentNum.setText(commentNum + " " + mCommentString);
                }
                List<VideoCommentBean> list = JSON.parseArray(obj.getString("commentlist"), VideoCommentBean.class);
                List<VideoCommentBean> newList = new ArrayList<>();
                for (VideoCommentBean bean : list) {
                    newList.add(bean);
                    VideoCommentBean firstChild = bean.getFirstChild();
                    if (firstChild != null) {
                        if (bean.getReplyNum() > 1) {
                            firstChild.setChildType(VideoCommentBean.CHILD_FIRST);
                            firstChild.setExpand(false);
                        } else {
                            firstChild.setChildType(VideoCommentBean.CHILD_NORMAL);
                        }
                        firstChild.setParentComment(bean);
                        newList.add(firstChild);
                    }
                }
                return newList;
            }

            @Override
            public void onRefresh(List<VideoCommentBean> list) {

            }

            @Override
            public void onNoData(boolean noData) {

            }

            @Override
            public void onLoadDataCompleted(int dataCount) {

            }
        });
    }

    public void setVideoBean(VideoBean videoBean) {
        if (mVideoBean != null) {
            String curVideoId = mVideoBean.getId();
            if (!TextUtils.isEmpty(curVideoId) && !curVideoId.equals(videoBean.getId())) {
                if (mVideoCommentAdapter != null) {
                    mVideoCommentAdapter.clearData();
                }
                if (mRefreshView != null) {
                    mRefreshView.showLoading();
                }
            }
        }
        mVideoBean = videoBean;
    }

    public void showBottom() {
        if (mAnimating) {
            return;
        }
        if (mRoot != null && mRoot.getVisibility() != View.VISIBLE) {
            mRoot.setVisibility(View.VISIBLE);
        }
        if (mShowAnimator != null) {
            mShowAnimator.start();
        }
    }

    public void hideBottom() {
        if (mAnimating) {
            return;
        }
        if (mHideAnimator != null) {
            mHideAnimator.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.root:
            case R.id.btn_close:
                hideBottom();
                break;
            case R.id.input:
                ((VideoPlayActivity) mContext).openCommentInputWindow(false, mVideoBean, null);
                break;
            case R.id.btn_face:
                ((VideoPlayActivity) mContext).openCommentInputWindow(true, mVideoBean, null);
                break;
        }
    }


    public void release() {
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
        }
        mShowAnimator = null;
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
        }
        mHideAnimator = null;
        HttpUtil.cancel(HttpConsts.GET_VIDEO_COMMENT_LIST);
        HttpUtil.cancel(HttpConsts.SET_COMMENT_LIKE);
        HttpUtil.cancel(HttpConsts.GET_COMMENT_REPLY);
    }

    @Override
    public void onItemClick(VideoCommentBean bean, int position) {
        ((VideoPlayActivity) mContext).openCommentInputWindow(false, mVideoBean, bean);
    }

}
