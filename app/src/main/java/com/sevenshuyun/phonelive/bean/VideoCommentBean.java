package com.sevenshuyun.phonelive.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.utils.WordUtil;

import java.util.List;


/**
 * Created by cxf on 2017/7/14.
 */

public class VideoCommentBean implements Parcelable {

    public static final int CHILD_NOT = 0;//不是子评论
    public static final int CHILD_NORMAL = 1;//是子评论，但不是头或尾
    public static final int CHILD_FIRST = 2;//是子评论,具有展开按钮
    public static final int CHILD_LAST = 3;//是子评论,具有收起按钮
    private static final String REPLY = WordUtil.getString(R.string.video_comment_reply) + " ";

    private String id;
    private String uid;
    private String toUid;
    private String videoId;
    private String commentId;
    private String parentId;
    private String content;
    private String addTime;
    private String atInfo;
    private UserBean userBean;
    private String likeNum;
    private int like;
    private int replyNum;
    private String datetime;
    private UserBean toUserBean;
    private ToCommentInfo toCommentInfo;
    private boolean expand;
    private int childType;
    private VideoCommentBean parentComment;
    private List<VideoCommentBean> mChildList;
    private int mChildPage = 1;


    public VideoCommentBean() {

    }

    @JSONField(name = "islike")
    public int getLike() {
        return like;
    }

    @JSONField(name = "islike")
    public void setLike(int like) {
        this.like = like;
    }

    @JSONField(name = "tocommentinfo")
    public ToCommentInfo getToCommentInfo() {
        return toCommentInfo;
    }

    @JSONField(name = "tocommentinfo")
    public void setToCommentInfo(ToCommentInfo toCommentInfo) {
        this.toCommentInfo = toCommentInfo;
    }

    @JSONField(name = "replys")
    public int getReplyNum() {
        return replyNum;
    }

    @JSONField(name = "replys")
    public void setReplyNum(int replyNum) {
        this.replyNum = replyNum;
    }

    @JSONField(name = "likes")
    public String getLikeNum() {
        return likeNum;
    }

    @JSONField(name = "likes")
    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @JSONField(name = "touid")
    public String getToUid() {
        return toUid;
    }

    @JSONField(name = "touid")
    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    @JSONField(name = "videoid")
    public String getVideoId() {
        return videoId;
    }

    @JSONField(name = "videoid")
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    @JSONField(name = "commentid")
    public String getCommentId() {
        return commentId;
    }

    @JSONField(name = "commentid")
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        if (this.childType != CHILD_NOT && this.toUserBean != null && !TextUtils.isEmpty(this.toUserBean.getId())) {
            String userName = toUserBean.getUserNiceName();
            if (!TextUtils.isEmpty(userName)) {
                return REPLY + userName + ":" + this.content;
            }
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JSONField(name = "addtime")
    public String getAddTime() {
        return addTime;
    }

    @JSONField(name = "addtime")
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    @JSONField(name = "userinfo")
    public UserBean getUserBean() {
        return userBean;
    }

    @JSONField(name = "userinfo")
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @JSONField(name = "touserinfo")
    public UserBean getToUserBean() {
        return toUserBean;
    }

    @JSONField(name = "touserinfo")
    public void setToUserBean(UserBean toUserBean) {
        this.toUserBean = toUserBean;
    }

    @JSONField(name = "at_info")
    public String getAtInfo() {
        return atInfo;
    }

    @JSONField(name = "at_info")
    public void setAtInfo(String atInfo) {
        this.atInfo = atInfo;
    }

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }


    public int getChildType() {
        return childType;
    }

    public void setChildType(int childType) {
        this.childType = childType;
    }

    public VideoCommentBean getParentComment() {
        return parentComment;
    }

    public void setParentComment(VideoCommentBean parentComment) {
        this.parentComment = parentComment;
    }

    @JSONField(name = "replylist")
    public List<VideoCommentBean> getChildList() {
        return mChildList;
    }

    @JSONField(name = "replylist")
    public void setChildList(List<VideoCommentBean> childList) {
        mChildList = childList;
    }

    public static class ToCommentInfo implements Parcelable {
        public ToCommentInfo() {

        }

        private String content;
        private String at_info;

        protected ToCommentInfo(Parcel in) {
            content = in.readString();
            at_info = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(content);
            dest.writeString(at_info);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ToCommentInfo> CREATOR = new Creator<ToCommentInfo>() {
            @Override
            public ToCommentInfo createFromParcel(Parcel in) {
                return new ToCommentInfo(in);
            }

            @Override
            public ToCommentInfo[] newArray(int size) {
                return new ToCommentInfo[size];
            }
        };

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAt_info() {
            return at_info;
        }

        public void setAt_info(String at_info) {
            this.at_info = at_info;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uid);
        dest.writeString(this.toUid);
        dest.writeString(this.videoId);
        dest.writeString(this.commentId);
        dest.writeString(this.parentId);
        dest.writeString(this.content);
        dest.writeString(this.addTime);
        dest.writeParcelable(this.userBean, flags);
        dest.writeString(this.likeNum);
        dest.writeInt(this.replyNum);
        dest.writeString(this.datetime);
        dest.writeParcelable(this.toUserBean, flags);
        dest.writeParcelable(this.toCommentInfo, flags);
        dest.writeInt(this.like);
        dest.writeString(this.atInfo);
    }


    protected VideoCommentBean(Parcel in) {
        this.id = in.readString();
        this.uid = in.readString();
        this.toUid = in.readString();
        this.videoId = in.readString();
        this.commentId = in.readString();
        this.parentId = in.readString();
        this.content = in.readString();
        this.addTime = in.readString();
        this.userBean = in.readParcelable(UserBean.class.getClassLoader());
        this.likeNum = in.readString();
        this.replyNum = in.readInt();
        this.datetime = in.readString();
        this.toUserBean = in.readParcelable(UserBean.class.getClassLoader());
        this.toCommentInfo = in.readParcelable(ToCommentInfo.class.getClassLoader());
        this.like = in.readInt();
        this.atInfo = in.readString();
    }

    public static final Creator<VideoCommentBean> CREATOR = new Creator<VideoCommentBean>() {
        @Override
        public VideoCommentBean createFromParcel(Parcel source) {
            return new VideoCommentBean(source);
        }

        @Override
        public VideoCommentBean[] newArray(int size) {
            return new VideoCommentBean[size];
        }
    };

    /**
     * 获取第一条子评论
     */
    public VideoCommentBean getFirstChild() {
        if (mChildList != null && mChildList.size() > 0) {
            return mChildList.get(0);
        }
        return null;
    }

    public void addChild(List<VideoCommentBean> childList) {
        if (mChildList != null) {
            mChildList.addAll(childList);
        }
    }

    public void removeChild() {
        if (mChildList != null && mChildList.size() > 1) {
            mChildList = mChildList.subList(0, 1);
        }
    }

    public int getChildCount() {
        if (mChildList != null) {
            return mChildList.size();
        }
        return 0;
    }

    public int getChildPage() {
        return mChildPage;
    }

    public void setChildPage(int childPage) {
        mChildPage = childPage;
    }
}
