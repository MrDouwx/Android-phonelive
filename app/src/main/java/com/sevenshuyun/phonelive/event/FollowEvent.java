package com.sevenshuyun.phonelive.event;

/**
 * Created by cxf on 2018/9/28.
 */

public class FollowEvent {

    private int mFrom;
    private String mToUid;
    private int mIsAttention;

    public FollowEvent(int from, String toUid, int isAttention) {
        mFrom = from;
        mToUid = toUid;
        mIsAttention = isAttention;
    }

    public int getFrom() {
        return mFrom;
    }

    public void setFrom(int from) {
        mFrom = from;
    }

    public String getToUid() {
        return mToUid;
    }

    public void setToUid(String toUid) {
        mToUid = toUid;
    }

    public int getIsAttention() {
        return mIsAttention;
    }

    public void setIsAttention(int isAttention) {
        mIsAttention = isAttention;
    }
}
