package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.view.ViewGroup;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.custom.RefreshView;
import com.sevenshuyun.phonelive.interfaces.AppBarStateListener;
import com.sevenshuyun.phonelive.interfaces.DataLoader;
import com.sevenshuyun.phonelive.interfaces.LifeCycleListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarExpandListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarLayoutListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity中的首页，附近，排行，我的 的子页面
 */

public abstract class AbsMainChildViewHolder extends AbsMainViewHolder implements DataLoader {

    protected AppBarLayout mAppBarLayout;
    protected MainAppBarLayoutListener mAppBarLayoutListener;
    protected MainAppBarExpandListener mAppBarExpandListener;
    protected boolean mAppBarExpand = true;//AppBarLayout是否展开
    protected boolean mNeedDispatch;//是否需要执行AppBarLayoutListener的回调
    protected RefreshView mRefreshView;

    public AbsMainChildViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public void init() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        if (mAppBarLayout != null) {
            mAppBarLayout.addOnOffsetChangedListener(new AppBarStateListener() {

                @Override
                public void onStateChanged(AppBarLayout appBarLayout, int state) {
                    switch (state) {
                        case AppBarStateListener.EXPANDED:
                            mAppBarExpand = true;
                            if (mAppBarExpandListener != null) {
                                mAppBarExpandListener.onExpand(true);
                            }
                            break;
                        case AppBarStateListener.MIDDLE:
                        case AppBarStateListener.COLLAPSED:
                            mAppBarExpand = false;
                            if (mAppBarExpandListener != null) {
                                mAppBarExpandListener.onExpand(false);
                            }
                            break;
                    }
                }
            });
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    float totalScrollRange = appBarLayout.getTotalScrollRange();
                    float rate = -1 * verticalOffset / totalScrollRange;
                    if (mNeedDispatch && mAppBarLayoutListener != null) {
                        mAppBarLayoutListener.onOffsetChanged(rate);
                    }
                }
            });
        }
    }

    /**
     * 设置AppBarLayout滑动监听
     */
    public void setAppBarLayoutListener(MainAppBarLayoutListener appBarLayoutListener) {
        mAppBarLayoutListener = appBarLayoutListener;
    }

    /**
     * 设置AppBarLayout展开监听
     */
    public void setAppBarExpandListener(MainAppBarExpandListener appBarExpandListener) {
        mAppBarExpandListener = appBarExpandListener;
    }

    /**
     * AppBarLayout 展开
     */
    public void expand() {
        if (!mAppBarExpand && mAppBarLayout != null) {
            mAppBarLayout.setExpanded(true);
        }
    }

    @Override
    public List<LifeCycleListener> getLifeCycleListenerList() {
        List<LifeCycleListener> list = new ArrayList<>();
        if (mLifeCycleListener != null) {
            list.add(mLifeCycleListener);
        }
        return list;
    }


    public void setCanRefresh(boolean canRefresh) {
        if (mRefreshView != null) {
            mRefreshView.setRefreshEnable(canRefresh);
        }
    }


    public void setNeedDispatch(boolean needDispatch) {
        mNeedDispatch = needDispatch;
    }
}
