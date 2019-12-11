package com.sevenshuyun.phonelive.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.LevelBean;
import com.sevenshuyun.phonelive.bean.LiveReceiveGiftBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.utils.DpUtil;
import com.sevenshuyun.phonelive.utils.TextRender;

/**
 * Created by cxf on 2018/10/13.
 */

public class LiveGiftViewHolder extends AbsViewHolder {

    private ImageView mAvatar;
    private TextView mName;
    private TextView mContent;
    private ImageView mGiftIcon;
    private TextView mGiftCount;
    private int mDp400;
    private View mGroup1;
    private View mGroup2;
    private ObjectAnimator mAnimator1;
    private ObjectAnimator mAnimator2;
    private Animation mAnimation1;//礼物图标执行的放大动画
    private Animation mAnimation2;//礼物数字执行的放大动画
    private boolean mIdle;//是否空闲
    private boolean mShowed;//展示礼物的控件是否显示出来了
    private String mLastUid;//上次送礼物人的uid
    private String mLastGiftId;//上次送的礼物的id
    private int mLastGiftCount;//上次送的礼物的个数
    private int mLianCount;//连送礼物的个数

    public LiveGiftViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_gift;
    }

    @Override
    public void init() {
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mContent = (TextView) findViewById(R.id.content);
        mGiftIcon = (ImageView) findViewById(R.id.gift_icon);
        mGiftCount = (TextView) findViewById(R.id.gift_count);
        mDp400 = DpUtil.dp2px(400);
        mGroup1 = findViewById(R.id.group_1);
        mGroup2 = findViewById(R.id.group_2);
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        mAnimator1 = ObjectAnimator.ofFloat(mGroup1, "translationX", 0);
        mAnimator1.setDuration(800);
        mAnimator1.setInterpolator(interpolator);
        mAnimator2 = ObjectAnimator.ofFloat(mGroup2, "translationX", 0);
        mAnimator2.setDuration(800);
        mAnimator2.setInterpolator(interpolator);
        mAnimation1 = new ScaleAnimation(0.8f, 1.3f, 0.8f, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.8f);
        mAnimation1.setDuration(300);
        mAnimation1.setInterpolator(interpolator);
        mAnimation2 = new ScaleAnimation(0.7f, 1.3f, 0.7f, 1.3f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1f);
        mAnimation2.setDuration(300);
        mAnimation2.setInterpolator(interpolator);
        mIdle = true;
    }

    /**
     * 显示礼物动画
     */
    public void show(LiveReceiveGiftBean bean, boolean isSameUser) {
        mIdle = false;
        boolean lian = true;
        if (!isSameUser) {
            ImgLoader.displayAvatar(bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserNiceName());
            LevelBean levelBean = AppConfig.getInstance().getLevel(bean.getLevel());
            if (levelBean != null) {
                mName.setTextColor(Color.parseColor(levelBean.getColor()));
            }
            lian = false;
        }
        if (TextUtils.isEmpty(mLastGiftId) || !mLastGiftId.equals(bean.getGiftId())) {
            ImgLoader.display(bean.getGiftIcon(), mGiftIcon);
            mContent.setText(TextRender.renderGiftInfo(bean.getGiftCount(), bean.getGiftName()));
            lian = false;
        } else {
            if (mLastGiftCount != bean.getGiftCount()) {
                mContent.setText(TextRender.renderGiftInfo(bean.getGiftCount(), bean.getGiftName()));
                lian = false;
            }
        }
        if (lian) {
            mLianCount++;
        } else {
            mLianCount = bean.getLianCount();
        }
        mGiftCount.setText(TextRender.renderGiftCount(mLianCount));
        mLastUid = bean.getUid();
        mLastGiftId = bean.getGiftId();
        mLastGiftCount = bean.getGiftCount();
        if (!mShowed) {
            mShowed = true;
            mAnimator1.start();
            mAnimator2.start();
        }
        if (lian) {
            mGiftIcon.startAnimation(mAnimation1);
            mGiftCount.startAnimation(mAnimation2);
        }
    }

    public void hide() {
        if (mGroup1 != null) {
            mGroup1.setTranslationX(-mDp400);
        }
        if (mGroup2 != null) {
            mGroup2.setTranslationX(mDp400);
        }
        mAvatar.setImageDrawable(null);
        mGiftIcon.setImageDrawable(null);
        mIdle = true;
        mShowed = false;
        mLastUid = null;
        mLastGiftId = null;
        mLastGiftCount = 0;
    }

    /**
     * 是否是空闲的
     */
    public boolean isIdle() {
        return mIdle;
    }

    /**
     * 是否同一个人送
     */
    public boolean isSameUser(LiveReceiveGiftBean bean) {
        return !TextUtils.isEmpty(mLastUid) && mLastUid.equals(bean.getUid());
    }


    public void cancelAnimAndHide(){
        cancelAnim();
        hide();
    }

    private void cancelAnim(){
        if (mAnimator1 != null) {
            mAnimator1.cancel();
        }
        if (mAnimator2 != null) {
            mAnimator2.cancel();
        }
        if (mGiftCount != null) {
            mGiftCount.clearAnimation();
        }
    }

    public void release() {
        cancelAnim();
        mContext = null;
        mParentView = null;
        mLastUid = null;
        mLastGiftId = null;
    }
}
