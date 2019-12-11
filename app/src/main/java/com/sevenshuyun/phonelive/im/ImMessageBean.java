package com.sevenshuyun.phonelive.im;

import java.io.File;

import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by cxf on 2018/7/12.
 * IM 消息实体类
 */

public class ImMessageBean {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_VOICE = 3;
    public static final int TYPE_LOCATION = 4;

    private String uid;
    private Message rawMessage;
    private int type;
    private boolean fromSelf;
    private long time;
    private File imageFile;
    private boolean loading;
    private boolean sendFail;

    public ImMessageBean(String uid, Message rawMessage, int type, boolean fromSelf) {
        this.uid = uid;
        this.rawMessage = rawMessage;
        this.type = type;
        this.fromSelf = fromSelf;
        time = rawMessage.getCreateTime();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Message getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(Message rawMessage) {
        this.rawMessage = rawMessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isFromSelf() {
        return fromSelf;
    }

    public void setFromSelf(boolean fromSelf) {
        this.fromSelf = fromSelf;
    }

    public long getTime() {
        return time;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isSendFail() {
        return sendFail;
    }

    public void setSendFail(boolean sendFail) {
        this.sendFail = sendFail;
    }

    public int getVoiceDuration() {
        int duration = 0;
        if (rawMessage != null) {
            MessageContent content = rawMessage.getContent();
            if (content != null) {
                VoiceContent voiceContent = (VoiceContent) content;
                duration = voiceContent.getDuration();
            }
        }
        return duration;
    }

    public boolean isRead() {
        return rawMessage != null && rawMessage.haveRead();
    }

    public int getMessageId() {
        return rawMessage != null ? rawMessage.getId() : -1;
    }

    public void hasRead(BasicCallback callback) {
        if (rawMessage != null) {
            rawMessage.setHaveRead(callback);
        }
    }
}
