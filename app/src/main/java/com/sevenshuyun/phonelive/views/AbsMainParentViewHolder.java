package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.custom.MyViewPager;
import com.sevenshuyun.phonelive.custom.ViewPagerIndicator;
import com.sevenshuyun.phonelive.interfaces.LifeCycleListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarLayoutListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/26.
 * MainActivity 中的首页，附近 的父页面 的基类
 */

public abstract class AbsMainParentViewHolder extends AbsMainViewHolder {

    protected ViewGroup mTopContainer; //放置头部导航条的容器
    protected AbsMainChildTopViewHolder[] mViewHolders;
    protected MyViewPager mViewPager;
    protected View mTopView;
    protected TextView mRedPoint;//显示未读消息数量的红点
    protected ViewPagerIndicator mIndicator;
    protected boolean mFirst = true;

    public AbsMainParentViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public void init() {
        mTopContainer = (ViewGroup) findViewById(R.id.top_container);
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(3);
        mTopView = LayoutInflater.from(mContext).inflate(R.layout.view_main_home_top, null, false);
        mRedPoint = (TextView) mTopView.findViewById(R.id.red_point);
        mIndicator = (ViewPagerIndicator) mTopView.findViewById(R.id.indicator);
        mIndicator.setListener(new ViewPagerIndicator.OnPageChangeListener() {
            @Override
            public void onTabClick(int position) {
                for (AbsMainChildTopViewHolder vh : mViewHolders) {
                    vh.expand();
                }
                if (mViewHolders != null) {
                    mViewHolders[position].removeTopView();
                }
                addTopView(mTopView);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mFirst) {
                    mFirst = false;
                } else {
                    addTopView(mTopView);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mViewHolders != null) {
                    for (int i = 0, length = mViewHolders.length; i < length; i++) {
                        if (position == i) {
                            mViewHolders[i].setNeedDispatch(true);
                            mViewHolders[i].loadData();
                        } else {
                            mViewHolders[i].setNeedDispatch(false);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 0) {
                    removeTopView();
                    if (mViewHolders != null) {
                        mViewHolders[mViewPager.getCurrentItem()].addTopView(mTopView);
                    }
                }
            }
        });
    }

    public void addTopView(View view) {
        if (view != null && mTopContainer != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                if (parent != mTopContainer) {
                    ((ViewGroup) parent).removeView(view);
                    mTopContainer.addView(view);
                }
            } else {
                mTopContainer.addView(view);
            }
        }
    }

    public void removeTopView() {
        if (mTopContainer != null && mTopContainer.getChildCount() > 0) {
            mTopContainer.removeAllViews();
        }
    }

    @Override
    public void setAppBarLayoutListener(MainAppBarLayoutListener appBarLayoutListener) {
        if (mViewHolders != null) {
            for (AbsMainChildTopViewHolder vh : mViewHolders) {
                vh.setAppBarLayoutListener(appBarLayoutListener);
            }
        }
    }

    @Override
    public List<LifeCycleListener> getLifeCycleListenerList() {
        List<LifeCycleListener> list = new ArrayList<>();
        if(mLifeCycleListener!=null){
            list.add(mLifeCycleListener);
        }
        for (AbsMainChildTopViewHolder vh : mViewHolders) {
            LifeCycleListener listener = vh.getLifeCycleListener();
            if (listener != null) {
                list.add(listener);
            }
        }
        return list;
    }

    @Override
    public void loadData() {
        mViewHolders[mViewPager.getCurrentItem()].loadData();
    }

    /**
     * 显示未读消息
     */
    public void setUnReadCount(String unReadCount) {
        if (mRedPoint != null) {
            if ("0".equals(unReadCount)) {
                if (mRedPoint.getVisibility() == View.VISIBLE) {
                    mRedPoint.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mRedPoint.getVisibility() != View.VISIBLE) {
                    mRedPoint.setVisibility(View.VISIBLE);
                }
            }
            mRedPoint.setText(unReadCount);
        }
    }

    @Override
    public void setShowed(boolean showed) {
        super.setShowed(showed);
        if (showed) {
            for (int i = 0, length = mViewHolders.length; i < length; i++) {
                if (i == mViewPager.getCurrentItem()) {
                    mViewHolders[i].setNeedDispatch(true);
                } else {
                    mViewHolders[i].setNeedDispatch(false);
                }
            }
        } else {
            for (AbsMainChildTopViewHolder vh : mViewHolders) {
                vh.setNeedDispatch(false);
            }
        }
    }
}
