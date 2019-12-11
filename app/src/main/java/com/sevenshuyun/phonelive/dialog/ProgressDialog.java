package com.sevenshuyun.phonelive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.custom.ProgressTextView3;

/**
 * Created by cxf on 2018/12/1.
 */

public class ProgressDialog extends Dialog {

    private ProgressTextView3 mProgress;

    public ProgressDialog(@NonNull Context context) {
        super(context);
    }

    public ProgressDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);

    }

    public ProgressDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mProgress = findViewById(R.id.progress);
    }

    public void setProgress(int progress) {
        if (mProgress != null) {
            mProgress.setProgress(progress);
        }
    }

}
