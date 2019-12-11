package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.LiveBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.utils.IconUtil;

/**
 * Created by cxf on 2018/9/26.
 */

public class MainHomeHotAdapter extends RefreshAdapter<LiveBean> {


    private View.OnClickListener mOnClickListener;

    public MainHomeHotAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!canClick()){
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mList.get(position), position);
                    }
                }
            }
        };
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_main_home_live, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mCover;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mNum;
        ImageView mType;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mNum = (TextView) itemView.findViewById(R.id.num);
            mType = (ImageView) itemView.findViewById(R.id.type);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(LiveBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(bean.getThumb(), mCover);
            ImgLoader.display(bean.getAvatar(), mAvatar);
            mName.setText(bean.getUserNiceName());
            if (TextUtils.isEmpty(bean.getTitle())) {
                if (mTitle.getVisibility() == View.VISIBLE) {
                    mTitle.setVisibility(View.GONE);
                }
            } else {
                if (mTitle.getVisibility() != View.VISIBLE) {
                    mTitle.setVisibility(View.VISIBLE);
                }
                mTitle.setText(bean.getTitle());
            }
            mNum.setText(bean.getNums());
            mType.setImageResource(IconUtil.getLiveTypeIcon(bean.getType()));
        }
    }

}
