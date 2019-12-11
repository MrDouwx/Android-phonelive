package com.sevenshuyun.phonelive.glide;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sevenshuyun.phonelive.AppContext;
import com.sevenshuyun.phonelive.R;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by cxf on 2017/8/9.
 */

public class ImgLoader {
    private static RequestManager sManager;
    private static BlurTransformation sBlurTransformation;

    static {
        sManager = Glide.with(AppContext.sInstance);
        sBlurTransformation = new BlurTransformation(AppContext.sInstance, 10);
    }

    public static void display(String url, ImageView imageView) {
        sManager.load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
    }

    public static void displayWithError(String url, ImageView imageView, int errorRes) {
        sManager.load(url).error(errorRes).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
    }

    public static void displayAvatar(String url, ImageView imageView) {
        displayWithError(url, imageView, R.mipmap.icon_avatar_placeholder);
    }

    public static void display(File file, ImageView imageView) {
        sManager.load(file).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
    }

    public static void display(int res, ImageView imageView) {
        sManager.load(res).into(imageView);
    }

    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(String videoPath, ImageView imageView) {
        sManager.load(Uri.fromFile(new File(videoPath))).into(imageView);
    }

    public static void displayDrawable(String url, final DrawableCallback callback) {
        sManager.load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (callback != null) {
                    callback.callback(resource);
                }
            }
        });
    }

    public static void displayDrawable(File file, final DrawableCallback callback) {
        sManager.load(file).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (callback != null) {
                    callback.callback(resource);
                }
            }
        });
    }


    public static void display(String url, ImageView imageView, int placeholderRes) {
        sManager.load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(placeholderRes).into(imageView);
    }

    /**
     * 显示模糊的毛玻璃图片
     */
    public static void displayBlur(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(sBlurTransformation)
                .into(imageView);
    }


    public interface DrawableCallback {
        void callback(Drawable drawable);
    }


}
