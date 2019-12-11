package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.view.ViewGroup;

import com.sevenshuyun.phonelive.interfaces.LifeCycleListener;
import com.sevenshuyun.phonelive.interfaces.MainAppBarLayoutListener;

import java.util.List;

/**
 * Created by cxf on 2018/10/26.
 */

public abstract class AbsMainViewHolder extends AbsViewHolder {

    protected boolean mFirstLoadData = true;
    protected boolean mShowed;//是否切换到了当前页面

    public AbsMainViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public void setAppBarLayoutListener(MainAppBarLayoutListener appBarLayoutListener) {
    }


    public List<LifeCycleListener> getLifeCycleListenerList() {
        return null;
    }

    public void loadData() {
    }

    protected boolean isFirstLoadData() {
        if (mFirstLoadData) {
            mFirstLoadData = false;
            return true;
        }
        return false;
    }

    public void setShowed(boolean showed) {
        mShowed = showed;
    }
}
