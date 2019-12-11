package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.Constants;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.UserBean;
import com.sevenshuyun.phonelive.bean.VideoCommentBean;
import com.sevenshuyun.phonelive.glide.ImgLoader;
import com.sevenshuyun.phonelive.http.HttpCallback;
import com.sevenshuyun.phonelive.http.HttpConsts;
import com.sevenshuyun.phonelive.http.HttpUtil;
import com.sevenshuyun.phonelive.utils.TextRender;
import com.sevenshuyun.phonelive.utils.ToastUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/3.
 */

public class VideoCommentAdapter extends RefreshAdapter<VideoCommentBean> {

    private Drawable mLikeDrawable;
    private Drawable mUnLikeDrawable;
    private int mLikeColor;
    private int mUnLikeColor;
    private ScaleAnimation mLikeAnimation;
    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mLikeClickListener;
    private View.OnClickListener mExpandClickListener;
    private View.OnClickListener mCollapsedClickListener;
    private ImageView mCurLikeImageView;
    private int mCurLikeCommentPosition;
    private VideoCommentBean mCurLikeCommentBean;
    private HttpCallback mLikeCommentCallback;

    public VideoCommentAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                VideoCommentBean commentBean = mList.get(position);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(commentBean, position);
                }
            }
        };
        mLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_1);
        mUnLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_0);
        mLikeColor = 0xffff0000;
        mUnLikeColor = 0xffc8c8c8;
        mLikeAnimation = new ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mLikeAnimation.setDuration(200);
        mLikeAnimation.setRepeatCount(1);
        mLikeAnimation.setRepeatMode(Animation.REVERSE);
        mLikeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mCurLikeCommentBean != null) {
                    if (mCurLikeImageView != null) {
                        mCurLikeImageView.setImageDrawable(mCurLikeCommentBean.getLike() == 1 ? mLikeDrawable : mUnLikeDrawable);
                    }
                }
            }
        });
        mLikeCommentCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0 && mCurLikeCommentBean != null) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    int like = obj.getIntValue("islike");
                    String likeNum = obj.getString("likes");
                    if (mCurLikeCommentBean != null) {
                        mCurLikeCommentBean.setLike(like);
                        mCurLikeCommentBean.setLikeNum(likeNum);
                        notifyItemChanged(mCurLikeCommentPosition, Constants.PAYLOAD);
                    }
                    if (mCurLikeImageView != null && mLikeAnimation != null) {
                        mCurLikeImageView.startAnimation(mLikeAnimation);
                    }
                }
            }
        };
        mLikeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                VideoCommentBean bean = mList.get(position);
                String uid = bean.getUid();
                if (!TextUtils.isEmpty(uid) && uid.equals(AppConfig.getInstance().getUid())) {
                    ToastUtil.show(R.string.video_comment_cannot_self);
                    return;
                }
                mCurLikeImageView = (ImageView) v;
                mCurLikeCommentPosition = position;
                mCurLikeCommentBean = bean;
                HttpUtil.setCommentLike(bean.getId(), mLikeCommentCallback);
            }
        };
        mExpandClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                final int position = (int) tag;
                final VideoCommentBean bean = mList.get(position);
                final VideoCommentBean parentComment = bean.getParentComment();
                if (parentComment == null) {
                    return;
                }
                HttpUtil.cancel(HttpConsts.GET_COMMENT_REPLY);
                HttpUtil.getCommentReply(parentComment.getId(), parentComment.getChildPage(), new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            List<VideoCommentBean> list = JSON.parseArray(Arrays.toString(info), VideoCommentBean.class);
                            if (list == null || list.size() == 0) {
                                return;
                            }
                            if (list.size() > 1) {
                                list = list.subList(1, list.size());
                            }
                            for (int i = 0, size = list.size(); i < size; i++) {
                                VideoCommentBean commentBean = list.get(i);
                                if (i < size - 1) {
                                    commentBean.setChildType(VideoCommentBean.CHILD_NORMAL);
                                }
                                commentBean.setParentComment(parentComment);
                            }
                            bean.setExpand(true);
                            parentComment.addChild(list);
                            VideoCommentBean lastCommentBean = list.get(list.size() - 1);
                            if (parentComment.getChildCount() == parentComment.getReplyNum()) {
                                lastCommentBean.setChildType(VideoCommentBean.CHILD_LAST);
                            } else {
                                lastCommentBean.setChildType(VideoCommentBean.CHILD_FIRST);
                                lastCommentBean.setExpand(false);
                                parentComment.setChildPage(parentComment.getChildPage() + 1);
                            }
                            mList.addAll(position + 1, list);
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        };
        mCollapsedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                VideoCommentBean bean = mList.get(position);
                VideoCommentBean parentComment = bean.getParentComment();
                if (parentComment == null) {
                    return;
                }
                VideoCommentBean firstChild = parentComment.getFirstChild();
                if (firstChild == null) {
                    return;
                }
                String firstChildId = firstChild.getId();
                if (TextUtils.isEmpty(firstChildId)) {
                    return;
                }
                for (int i = 0, size = mList.size(); i < size; i++) {
                    VideoCommentBean cb = mList.get(i);
                    if (firstChildId.equals(cb.getId())) {
                        cb.setExpand(false);
                        List<VideoCommentBean> childList = parentComment.getChildList();
                        if (childList == null || childList.size() <= 1) {
                            return;
                        }
                        childList = childList.subList(1, childList.size());
                        mList.removeAll(childList);
                        parentComment.removeChild();
                        parentComment.setChildPage(1);
                        int parentPosition = i - 1;
                        if (mRecyclerView != null && parentPosition >= 0) {
                            mRecyclerView.scrollToPosition(parentPosition);
                        }
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        };
    }


    @Override
    public int getItemViewType(int position) {
        VideoCommentBean bean = mList.get(position);
        if (bean != null) {
            return bean.getChildType();
        }
        return VideoCommentBean.CHILD_NOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VideoCommentBean.CHILD_FIRST:
                return new ChildVhFirst(mInflater.inflate(R.layout.item_video_comment_child_first, parent, false));
            case VideoCommentBean.CHILD_NORMAL:
                return new ChildVh(mInflater.inflate(R.layout.item_video_comment_child, parent, false));
            case VideoCommentBean.CHILD_LAST:
                return new ChildVhLast(mInflater.inflate(R.layout.item_video_comment_child_last, parent, false));
            default:
                return new Vh(mInflater.inflate(R.layout.item_video_comment, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        if (vh instanceof Vh) {
            ((Vh) vh).setData(mList.get(position), position, payload);
        } else {
            ((ChildVh) vh).setData(mList.get(position), position, payload);
        }
    }


    class ChildVh extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mContent;

        public ChildVh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mContent = (TextView) itemView.findViewById(R.id.content);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoCommentBean bean, int position, Object payload) {
            itemView.setTag(position);
            if (payload == null) {
                UserBean u = bean.getUserBean();
                if (u != null) {
                    mName.setText(u.getUserNiceName());
                }
                mContent.setText(TextRender.renderVideoComment(bean.getContent(), "  " + bean.getDatetime()));
            }
        }
    }

    class ChildVhLast extends ChildVh {

        View mBtnbCollapsed;//收起按钮

        public ChildVhLast(View itemView) {
            super(itemView);
            mBtnbCollapsed = itemView.findViewById(R.id.btn_collapsed);
            mBtnbCollapsed.setOnClickListener(mCollapsedClickListener);
        }

        void setData(VideoCommentBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            mBtnbCollapsed.setTag(position);
        }
    }

    class ChildVhFirst extends ChildVh {

        View mBtnExpand;//展开按钮

        public ChildVhFirst(View itemView) {
            super(itemView);
            mBtnExpand = itemView.findViewById(R.id.btn_expand);
            mBtnExpand.setOnClickListener(mExpandClickListener);
        }

        void setData(VideoCommentBean bean, int position, Object payload) {
            super.setData(bean, position, payload);
            mBtnExpand.setTag(position);
            if (!bean.isExpand()) {
                if (mBtnExpand.getVisibility() != View.VISIBLE) {
                    mBtnExpand.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnExpand.getVisibility() == View.VISIBLE) {
                    mBtnExpand.setVisibility(View.GONE);
                }
            }
        }
    }


    class Vh extends RecyclerView.ViewHolder {

        View mLine;
        ImageView mAvatar;
        TextView mName;
        TextView mContent;
        ImageView mBtnLike;
        TextView mLikeNum;


        public Vh(View itemView) {
            super(itemView);
            mLine = itemView.findViewById(R.id.line);
            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mName = (TextView) itemView.findViewById(R.id.name);
            mContent = (TextView) itemView.findViewById(R.id.content);
            mBtnLike = (ImageView) itemView.findViewById(R.id.btn_like);
            mLikeNum = (TextView) itemView.findViewById(R.id.like_num);
            itemView.setOnClickListener(mOnClickListener);
            mBtnLike.setOnClickListener(mLikeClickListener);
        }

        void setData(VideoCommentBean bean, int position, Object payload) {
            boolean like = bean.getLike() == 1;
            if (payload == null) {
                if (position == 0) {
                    if (mLine.getVisibility() == View.VISIBLE) {
                        mLine.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mLine.getVisibility() != View.VISIBLE) {
                        mLine.setVisibility(View.VISIBLE);
                    }
                }

                UserBean u = bean.getUserBean();
                if (u != null) {
                    ImgLoader.display(u.getAvatar(), mAvatar);
                    mName.setText(u.getUserNiceName());
                }
                mContent.setText(TextRender.renderVideoComment(bean.getContent(), "  " + bean.getDatetime()));
                mBtnLike.setImageDrawable(like ? mLikeDrawable : mUnLikeDrawable);
            }
            mBtnLike.setTag(position);
            itemView.setTag(position);
            mLikeNum.setText(bean.getLikeNum());
            mLikeNum.setTextColor(like ? mLikeColor : mUnLikeColor);
        }
    }

}
