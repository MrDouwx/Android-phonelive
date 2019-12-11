package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.ConfigBean;
import com.sevenshuyun.phonelive.beauty.DefaultEffectListener;
import com.sevenshuyun.phonelive.beauty.EffectListener;
import com.sevenshuyun.phonelive.beauty.TiBeautyEffectListener;
import com.sevenshuyun.phonelive.interfaces.ILivePushViewHolder;
import com.sevenshuyun.phonelive.interfaces.LifeCycleAdapter;
import com.sevenshuyun.phonelive.interfaces.LivePushListener;
import com.sevenshuyun.phonelive.utils.DpUtil;
import com.sevenshuyun.phonelive.utils.L;
import com.sevenshuyun.phonelive.utils.ToastUtil;

import cn.tillusory.sdk.TiSDKManager;
import cn.tillusory.sdk.TiSDKManagerBuilder;
import cn.tillusory.sdk.bean.TiDistortionEnum;
import cn.tillusory.sdk.bean.TiFilterEnum;
import cn.tillusory.sdk.bean.TiRockEnum;

/**
 * Created by cxf on 2018/12/22.
 */

public abstract class AbsLivePushViewHolder extends AbsViewHolder implements ILivePushViewHolder {

    protected final String TAG = getClass().getSimpleName();
    protected LivePushListener mLivePushListener;
    protected boolean mCameraFront;//是否是前置摄像头
    protected boolean mFlashOpen;//闪光灯是否开启了
    protected boolean mPaused;
    protected boolean mStartPush;
    protected ViewGroup mBigContainer;
    protected ViewGroup mSmallContainer;
    protected ViewGroup mLeftContainer;
    protected ViewGroup mRightContainer;
    protected ViewGroup mPkContainer;
    protected View mPreView;
    protected boolean mOpenCamera;//是否选择了相机
    protected EffectListener mEffectListener;//萌颜的效果监听
    protected TiSDKManager mTiSDKManager;//各种萌颜效果控制器

    //倒计时
    protected TextView mCountDownText;
    protected int mCountDownCount = 3;

    public AbsLivePushViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public AbsLivePushViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    @Override
    public void init() {
        mBigContainer = (ViewGroup) findViewById(R.id.big_container);
        mSmallContainer = (ViewGroup) findViewById(R.id.small_container);
        mLeftContainer = (ViewGroup) findViewById(R.id.left_container);
        mRightContainer = (ViewGroup) findViewById(R.id.right_container);
        mPkContainer = (ViewGroup) findViewById(R.id.pk_container);
        mCameraFront = true;
        if (AppConfig.TI_BEAUTY_ENABLE) {
            initBeauty();
            mEffectListener = new TiBeautyEffectListener() {
                @Override
                public void onFilterChanged(TiFilterEnum tiFilterEnum) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setFilterEnum(tiFilterEnum);
                    }
                }

                @Override
                public void onMeiBaiChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setSkinWhitening(progress);
                    }
                }

                @Override
                public void onMoPiChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setSkinBlemishRemoval(progress);
                    }
                }

                @Override
                public void onBaoHeChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setSkinSaturation(progress);
                    }
                }

                @Override
                public void onFengNenChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setSkinTenderness(progress);
                    }
                }

                @Override
                public void onBigEyeChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setEyeMagnifying(progress);
                    }
                }

                @Override
                public void onFaceChanged(int progress) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setChinSlimming(progress);
                    }
                }

                @Override
                public void onTieZhiChanged(String tieZhiName) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setSticker(tieZhiName);
                    }
                }

                @Override
                public void onHaHaChanged(TiDistortionEnum tiDistortionEnum) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setDistortionEnum(tiDistortionEnum);
                    }
                }

                @Override
                public void onRockChanged(TiRockEnum tiRockEnum) {
                    if (mTiSDKManager != null) {
                        mTiSDKManager.setRockEnum(tiRockEnum);
                    }
                }
            };
        } else {
            mEffectListener = getDefaultEffectListener();
        }
        mLifeCycleListener = new LifeCycleAdapter() {

            @Override
            public void onReStart() {
                if (mOpenCamera) {
                    mOpenCamera = false;
                    onCameraRestart();
                }
            }


            @Override
            public void onDestroy() {
                if (mTiSDKManager != null) {
                    mTiSDKManager.destroy();
                }
                L.e(TAG, "LifeCycle------>onDestroy");
            }
        };
    }


    /**
     * 初始化萌颜
     */
    private void initBeauty() {
        try {
            mTiSDKManager = new TiSDKManagerBuilder().build();
            mTiSDKManager.setBeautyEnable(true);
            mTiSDKManager.setFaceTrimEnable(true);
            ConfigBean configBean = AppConfig.getInstance().getConfig();
            if (configBean != null) {
                mTiSDKManager.setSkinWhitening(configBean.getBeautyMeiBai());//美白
                mTiSDKManager.setSkinBlemishRemoval(configBean.getBeautyMoPi());//磨皮
                mTiSDKManager.setSkinSaturation(configBean.getBeautyBaoHe());//饱和
                mTiSDKManager.setSkinTenderness(configBean.getBeautyFenNen());//粉嫩
                mTiSDKManager.setEyeMagnifying(configBean.getBeautyBigEye());//大眼
                mTiSDKManager.setChinSlimming(configBean.getBeautyFace());//瘦脸
            } else {
                mTiSDKManager.setSkinWhitening(0);//美白
                mTiSDKManager.setSkinBlemishRemoval(0);//磨皮
                mTiSDKManager.setSkinSaturation(0);//饱和
                mTiSDKManager.setSkinTenderness(0);//粉嫩
                mTiSDKManager.setEyeMagnifying(0);//大眼
                mTiSDKManager.setChinSlimming(0);//瘦脸
            }
            mTiSDKManager.setSticker("");
            mTiSDKManager.setFilterEnum(TiFilterEnum.NO_FILTER);
        } catch (Exception e) {
            mTiSDKManager = null;
            ToastUtil.show(R.string.beauty_init_error);
        }
    }


    /**
     * 开播的时候 3 2 1倒计时
     */
    protected void startCountDown() {
        ViewGroup parent = (ViewGroup) mContentView;
        mCountDownText = (TextView) LayoutInflater.from(mContext).inflate(R.layout.view_count_down, parent, false);
        parent.addView(mCountDownText);
        mCountDownText.setText(String.valueOf(mCountDownCount));
        ScaleAnimation animation = new ScaleAnimation(3, 1, 3, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setRepeatCount(2);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup parent = (ViewGroup) mCountDownText.getParent();
                if (parent != null) {
                    parent.removeView(mCountDownText);
                    mCountDownText = null;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                mCountDownCount--;
                mCountDownText.setText(String.valueOf(mCountDownCount));
            }
        });
        mCountDownText.startAnimation(animation);
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
        if (mPreView != null && mLeftContainer != null) {
            ViewParent parent = mPreView.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(mPreView);
            }
            int h = mPreView.getHeight() / 2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mPreView.getWidth() / 2, h);
            params.setMargins(0, (DpUtil.dp2px(250) - h) / 2, 0, 0);
            mPreView.setLayoutParams(params);
            mLeftContainer.addView(mPreView);
        }
    }

    @Override
    public void changeToBig() {
        if (mPreView != null && mBigContainer != null) {
            ViewParent parent = mPreView.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(mPreView);
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mPreView.setLayoutParams(layoutParams);
            mBigContainer.addView(mPreView);
        }
    }

    @Override
    public void setOpenCamera(boolean openCamera) {
        mOpenCamera = openCamera;
    }

    @Override
    public void setLivePushListener(LivePushListener livePushListener) {
        mLivePushListener = livePushListener;
    }

    @Override
    public EffectListener getEffectListener() {
        return mEffectListener;
    }

    protected abstract void onCameraRestart();

    protected abstract DefaultEffectListener getDefaultEffectListener();

    public abstract void onPause();

    public abstract void onResume();

    public abstract void onRelease();


    @Override
    public void pause() {
        mPaused = true;
        if (mStartPush) {
            onPause();
        }
    }

    @Override
    public void resume() {
        if (mPaused && mStartPush) {
            onResume();
        }
        mPaused = false;
    }


    public void release() {
        if (mCountDownText != null) {
            mCountDownText.clearAnimation();
        }
        mLivePushListener = null;
        onRelease();
    }

}
