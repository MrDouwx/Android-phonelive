package com.sevenshuyun.phonelive.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.ugc.TXVideoEditer;
import com.sevenshuyun.phonelive.AppConfig;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cxf on 2018/6/25.
 */

public class VideoEditUtil {

    private Random mRandom;
    private List<Bitmap> mList;
    private TXVideoEditer mVideoEditer;
    private static VideoEditUtil sInstance;

    private VideoEditUtil() {
        mRandom = new Random();
    }

    public static VideoEditUtil getInstance() {
        if (sInstance == null) {
            synchronized (VideoEditUtil.class) {
                if (sInstance == null) {
                    sInstance = new VideoEditUtil();
                }
            }
        }
        return sInstance;
    }

    public void addVideoBitmap(Bitmap bitmap) {
        if (mList != null) {
            mList.add(new SoftReference<>(bitmap).get());
        }
    }

    public List<Bitmap> getList() {
        return mList;
    }


    public void release() {
        if (mVideoEditer != null) {
            mVideoEditer.setVideoProcessListener(null);
            mVideoEditer.setThumbnailListener(null);
            mVideoEditer.setTXVideoReverseListener(null);
            mVideoEditer.setTXVideoPreviewListener(null);
            mVideoEditer.setVideoGenerateListener(null);
            mVideoEditer.release();
        }
        clearBitmapList();
        mVideoEditer = null;
        mList = null;
    }

    public void clearBitmapList() {
        if (mList != null) {
            for (Bitmap bitmap : mList) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            mList.clear();
        }
    }

    /**
     * 设置视频输出路径
     */
    public String generateVideoOutputPath() {
        String outputDir = AppConfig.VIDEO_PATH;
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        String videoName = DateFormatUtil.getVideoCurTimeString() + mRandom.nextInt(9999);
        return outputDir + "android_" + AppConfig.getInstance().getUid() + "_" + videoName + ".mp4";
    }

    public TXVideoEditer createVideoEditer(Context context, String videoPath) {
        mVideoEditer = new TXVideoEditer(context);
        mVideoEditer.setVideoPath(videoPath);
        mList = new ArrayList<>();
        return mVideoEditer;
    }

    public TXVideoEditer getVideoEditer() {
        return mVideoEditer;
    }

}
