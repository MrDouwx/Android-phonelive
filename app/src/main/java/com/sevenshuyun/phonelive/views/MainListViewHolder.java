package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.ViewPagerAdapter;
import com.sevenshuyun.phonelive.custom.ViewPagerIndicator;
import com.sevenshuyun.phonelive.event.FollowEvent;
import com.sevenshuyun.phonelive.interfaces.LifeCycleAdapter;
import com.sevenshuyun.phonelive.interfaces.LifeCycleListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarExpandListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarLayoutListener;
import com.sevenshuyun.phonelive.utils.ScreenDimenUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 排行
 */

public class MainListViewHolder extends AbsMainViewHolder implements View.OnClickListener {

    private AbsMainListViewHolder[] mViewHolders;
    private ViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private View mRadioGroupWrap;
    private int mScreenWidth;

    public MainListViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_list;
    }

    @Override
    public void init() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(3);
        mViewHolders = new AbsMainListViewHolder[2];
        mViewHolders[0] = new MainListProfitViewHolder(mContext, mViewPager);
        mViewHolders[1] = new MainListContributeViewHolder(mContext, mViewPager);
        List<View> list = new ArrayList<>();
        MainAppBarExpandListener expandListener = new MainAppBarExpandListener() {
            @Override
            public void onExpand(boolean expand) {
                if (mViewPager != null) {
                    mViewHolders[mViewPager.getCurrentItem()].setCanRefresh(expand);
                }
            }
        };
        for (AbsMainListViewHolder vh : mViewHolders) {
            vh.setAppBarExpandListener(expandListener);
            list.add(vh.getContentView());

        }
        mViewPager.setAdapter(new ViewPagerAdapter(list));
        mRadioGroupWrap = findViewById(R.id.radio_group_wrap);
        mScreenWidth = ScreenDimenUtil.getInstance().getScreenWdith();
        mIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
        mIndicator.setTitles(new String[]{
                WordUtil.getString(R.string.main_list_profit),
                WordUtil.getString(R.string.main_list_contribute)
        });
        mIndicator.setViewPager(mViewPager);
        mIndicator.setListener(new ViewPagerIndicator.OnPageChangeListener() {
            @Override
            public void onTabClick(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && positionOffset == 0) {
                    positionOffset = 1;
                }
                mRadioGroupWrap.setTranslationX(-positionOffset * mScreenWidth);
            }

            @Override
            public void onPageSelected(int position) {
                mViewHolders[position].loadData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        findViewById(R.id.btn_pro_day).setOnClickListener(this);
        findViewById(R.id.btn_pro_week).setOnClickListener(this);
        findViewById(R.id.btn_pro_month).setOnClickListener(this);
        findViewById(R.id.btn_pro_all).setOnClickListener(this);
        findViewById(R.id.btn_con_day).setOnClickListener(this);
        findViewById(R.id.btn_con_week).setOnClickListener(this);
        findViewById(R.id.btn_con_month).setOnClickListener(this);
        findViewById(R.id.btn_con_all).setOnClickListener(this);
        mLifeCycleListener = new LifeCycleAdapter() {
            @Override
            public void onCreate() {
                EventBus.getDefault().register(MainListViewHolder.this);
            }

            @Override
            public void onDestroy() {
                EventBus.getDefault().unregister(MainListViewHolder.this);
            }
        };
    }

    @Override
    public void setAppBarLayoutListener(MainAppBarLayoutListener appBarLayoutListener) {
//        if (mViewHolders != null) {
//            for (AbsMainListViewHolder vh : mViewHolders) {
//                vh.setAppBarLayoutListener(appBarLayoutListener);
//            }
//        }
    }

    @Override
    public void loadData() {
        mViewHolders[mViewPager.getCurrentItem()].loadData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pro_day:
                mViewHolders[0].refreshData(AbsMainListViewHolder.DAY);
                break;
            case R.id.btn_pro_week:
                mViewHolders[0].refreshData(AbsMainListViewHolder.WEEK);
                break;
            case R.id.btn_pro_month:
                mViewHolders[0].refreshData(AbsMainListViewHolder.MONTH);
                break;
            case R.id.btn_pro_all:
                mViewHolders[0].refreshData(AbsMainListViewHolder.TOTAL);
                break;
            case R.id.btn_con_day:
                mViewHolders[1].refreshData(AbsMainListViewHolder.DAY);
                break;
            case R.id.btn_con_week:
                mViewHolders[1].refreshData(AbsMainListViewHolder.WEEK);
                break;
            case R.id.btn_con_month:
                mViewHolders[1].refreshData(AbsMainListViewHolder.MONTH);
                break;
            case R.id.btn_con_all:
                mViewHolders[1].refreshData(AbsMainListViewHolder.TOTAL);
                break;
        }
    }

    @Override
    public List<LifeCycleListener> getLifeCycleListenerList() {
        List<LifeCycleListener> list = new ArrayList<>();
        if (mLifeCycleListener != null) {
            list.add(mLifeCycleListener);
        }
        for (AbsMainListViewHolder vh : mViewHolders) {
            LifeCycleListener listener = vh.getLifeCycleListener();
            if (listener != null) {
                list.add(listener);
            }
        }
        return list;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (e.getFrom() != Constants.FOLLOW_FROM_LIST) {
            for (AbsMainListViewHolder vh : mViewHolders) {
                vh.onFollowEvent(e.getToUid(), e.getIsAttention());
            }
        }
    }

}
