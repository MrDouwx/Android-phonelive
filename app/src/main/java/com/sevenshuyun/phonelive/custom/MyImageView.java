package com.sevenshuyun.phonelive.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;

/**
 * Created by cxf on 2018/6/7.
 */

public class MyImageView extends RoundedImageView {

    private File mFile;
    private int mMsgId;

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public int getMsgId() {
        return mMsgId;
    }

    public void setMsgId(int msgId) {
        mMsgId = msgId;
    }
}
