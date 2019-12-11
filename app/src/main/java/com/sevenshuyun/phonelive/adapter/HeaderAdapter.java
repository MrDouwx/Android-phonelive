package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by cxf on 2018/10/23.
 */

public abstract class HeaderAdapter extends RecyclerView.Adapter {

    protected static final int HEAD = -1;
    protected View mHeadView;
    protected HeadVh mHeadVh;
    protected LayoutInflater mInflater;
    protected RecyclerView mRecyclerView;
    protected int mScrollY;
    private View mContactView;
    private int mHeadHeight;

    public HeaderAdapter(Context context, int headHeight) {
        mHeadHeight = headHeight;
        mHeadView = new View(context);
        mHeadView.setLayoutParams(new ViewGroup.LayoutParams(0, headHeight));
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD;
        }
        return 0;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEAD) {
            if (mHeadVh == null) {
                mHeadVh = new HeadVh(mHeadView);
            }
            return mHeadVh;
        }
        return onCreateNormalViewHolder(parent, viewType);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        if (position != 0) {
            onBindNormalViewHolder(vh, position - 1, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return getNormalItemCount() + 1;
    }

    private class HeadVh extends RecyclerView.ViewHolder {

        HeadVh(View itemView) {
            super(itemView);
        }
    }

    protected abstract RecyclerView.ViewHolder onCreateNormalViewHolder(@NonNull ViewGroup parent, int viewType);

    protected abstract void onBindNormalViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads);

    protected abstract int getNormalItemCount();

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mScrollY += dy;
                if (mContactView != null) {
                    int targetScrollY = -mScrollY;
                    if (targetScrollY < -mHeadHeight) {
                        targetScrollY = -mHeadHeight;
                    }
                    if (targetScrollY > 0) {
                        targetScrollY = 0;
                    }
                    if (mContactView.getTranslationY() != targetScrollY) {
                        mContactView.setTranslationY(targetScrollY);
                    }
                }
            }
        });
    }

    public View getContactView() {
        return mContactView;
    }

    public void setContactView(View contactView) {
        mContactView = contactView;
    }
}
