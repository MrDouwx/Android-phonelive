package com.sevenshuyun.phonelive.views;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sevenshuyun.phonelive.AppConfig;
import com.sevenshuyun.phonelive.HtmlConfig;
import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.activity.LiveAudienceActivity;
import com.sevenshuyun.phonelive.activity.LiveContributeActivity;
import com.sevenshuyun.phonelive.utils.L;

/**
 * Created by cxf on 2018/10/15.
 * 直播间礼物贡献榜
 */

public class LiveContributeViewHolder extends AbsLivePageViewHolder implements View.OnClickListener {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private String mLiveUid;

    public LiveContributeViewHolder(Context context, ViewGroup parentView, String liveUid) {
        super(context, parentView);
        mLiveUid = liveUid;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_contribute;
    }

    @Override
    public void init() {
        super.init();
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = new WebView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(params);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.e("H5-------->" + url);
                view.loadUrl(url);
                return true;
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 70) {
                    if (mProgressBar.getVisibility() == View.VISIBLE) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ((LinearLayout) mContentView).addView(mWebView);
    }

    @Override
    public void loadData() {
        if (!mLoad) {
            mLoad = true;
            mWebView.loadUrl(HtmlConfig.LIVE_LIST + mLiveUid);
        } else {
            mWebView.reload();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mWebView != null) {
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
        }
    }

    @Override
    public void hide() {
        if (mContext instanceof LiveContributeActivity) {
            ((LiveContributeActivity) mContext).onBackPressed();
        } else {
            super.hide();
        }
    }

    @Override
    public void onHide() {
        if (AppConfig.LIVE_ROOM_SCROLL && mContext != null && mContext instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) mContext).setScrollFrozen(false);
        }
    }
}
