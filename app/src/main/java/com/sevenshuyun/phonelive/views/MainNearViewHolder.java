package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.adapter.ViewPagerAdapter;
import com.sevenshuyun.phonelive.interfaces.MainAppBarExpandListener;
import com.sevenshuyun.phonelive.utils.WordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 附近
 */

public class MainNearViewHolder extends AbsMainParentViewHolder {

    public MainNearViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_near;
    }

    @Override
    public void init() {
        super.init();
        mViewHolders = new AbsMainChildTopViewHolder[1];
        mViewHolders[0] = new MainNearNearViewHolder(mContext, mViewPager);
        MainAppBarExpandListener expandListener = new MainAppBarExpandListener() {
            @Override
            public void onExpand(boolean expand) {
                if (mViewPager != null) {
                    mViewPager.setCanScroll(expand);
                    mViewHolders[mViewPager.getCurrentItem()].setCanRefresh(expand);
                }
            }
        };
        List<View> list = new ArrayList<>();
        for (AbsMainChildTopViewHolder vh : mViewHolders) {
            vh.setAppBarExpandListener(expandListener);
            list.add(vh.getContentView());
        }
        mViewPager.setAdapter(new ViewPagerAdapter(list));
        mIndicator.setTitles(new String[]{
                WordUtil.getString(R.string.near)
        });
        mIndicator.setViewPager(mViewPager);
        mViewHolders[0].addTopView(mTopView);
    }

}
