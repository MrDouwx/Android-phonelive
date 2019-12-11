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
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.bean.VideoBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;

/**
 * Created by cxf on 2018/9/26.
 */

public class MainHomeVideoAdapter extends RefreshAdapter<VideoBean> {

    private static final int FIRST_LINE = 1;
    private static final int NORMAL = 0;

    private View.OnClickListener mOnClickListener;

    public MainHomeVideoAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
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

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 1) {
            return FIRST_LINE;
        }
        return NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(viewType == FIRST_LINE ? R.layout.item_main_home_video_2 : R.layout.item_main_home_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    /**
     * 删除视频
     */
    public void deleteVideo(String videoId) {
        if (TextUtils.isEmpty(videoId)) {
            return;
        }
        for (int i = 0, size = mList.size(); i < size; i++) {
            if (videoId.equals(mList.get(i).getId())) {
                notifyItemRemoved(i);
                break;
            }
        }
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mCover;
        ImageView mAvatar;
        TextView mName;
        TextView mTitle;
        TextView mNum;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mNum = (TextView) itemView.findViewById(R.id.num);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoBean bean, int position) {
            itemView.setTag(position);
            ImgLoader.display(bean.getThumb(), mCover);
            mTitle.setText(bean.getTitle());
            mNum.setText(bean.getViewNum());
            UserBean userBean = bean.getUserBean();
            if (userBean != null) {
                ImgLoader.display(userBean.getAvatar(), mAvatar);
                mName.setText(userBean.getUserNiceName());
            }
        }
    }

}
