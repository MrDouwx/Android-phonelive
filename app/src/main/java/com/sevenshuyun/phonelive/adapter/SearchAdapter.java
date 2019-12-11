package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.LevelBean;
import com.sevenshuyun.phonelive.bean.SearchUserBean;
import com.sevenshuyun.phonelive.custom.MyRadioButton;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.interfaces.CommonCallback;
import com.sevenshuyun.phonelive.utils.IconUtil;
import com.sevenshuyun.phonelive.utils.WordUtil;

import java.util.List;

/**
 * Created by cxf on 2018/9/29.
 */

public class SearchAdapter extends RefreshAdapter<SearchUserBean> {

    private View.OnClickListener mFollowClickListener;
    private View.OnClickListener mClickListener;
    private String mFollow;
    private String mFollowing;
    private int mFrom;
    private String mUid;

    public SearchAdapter(Context context, int from) {
        super(context);
        mFrom = from;
        mFollow = WordUtil.getString(R.string.follow);
        mFollowing = WordUtil.getString(R.string.following);
        mFollowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    final int position = (int) tag;
                    final SearchUserBean bean = mList.get(position);
                    HttpUtil.setAttention(mFrom, bean.getId(), new CommonCallback<Integer>() {
                        @Override
                        public void callback(Integer isAttention) {
                            if (isAttention != null) {
                                bean.setAttention(isAttention);
                                notifyItemChanged(position, Constants.PAYLOAD);
                            }
                        }
                    });
                }
            }
        };
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canClick()) {
                    return;
                }
                Object tag = v.getTag();
                if (tag != null) {
                    int position = (int) tag;
                    SearchUserBean bean = mList.get(position);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(bean, position);
                    }
                }
            }
        };
        mUid = AppConfig.getInstance().getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mAvatar;
        TextView mName;
        TextView mSign;
        ImageView mSex;
        ImageView mLevelAnchor;
        ImageView mLevel;
        MyRadioButton mBtnFollow;

        public Vh(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mSign = (TextView) itemView.findViewById(R.id.sign);
            mSex = (ImageView) itemView.findViewById(R.id.sex);
            mLevelAnchor = (ImageView) itemView.findViewById(R.id.level_anchor);
            mLevel = (ImageView) itemView.findViewById(R.id.level);
            mBtnFollow = (MyRadioButton) itemView.findViewById(R.id.btn_follow);
            itemView.setOnClickListener(mClickListener);
            mBtnFollow.setOnClickListener(mFollowClickListener);
        }

        void setData(SearchUserBean bean, int position, Object payload) {
            itemView.setTag(position);
            if (payload == null) {
                ImgLoader.displayAvatar(bean.getAvatar(), mAvatar);
                mName.setText(bean.getUserNiceName());
                mSign.setText(bean.getSignature());
                mSex.setImageResource(IconUtil.getSexIcon(bean.getSex()));
                LevelBean anchorLevelBean = AppConfig.getInstance().getAnchorLevel(bean.getLevelAnchor());
                if (anchorLevelBean != null) {
                    ImgLoader.display(anchorLevelBean.getThumb(), mLevelAnchor);
                }
                LevelBean levelBean = AppConfig.getInstance().getLevel(bean.getLevel());
                if (levelBean != null) {
                    ImgLoader.display(levelBean.getThumb(), mLevel);
                }
            }
            if (mUid.equals(bean.getId())) {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
                if (bean.getAttention() == 1) {
                    mBtnFollow.doChecked(true);
                    mBtnFollow.setText(mFollowing);
                } else {
                    mBtnFollow.doChecked(false);
                    mBtnFollow.setText(mFollow);
                }
                mBtnFollow.setTag(position);
            }
        }

    }
}
