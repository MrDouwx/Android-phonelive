package com.sevenshuyun.phonelive.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by cxf on 2018/10/12.
 */

public class MyRelativeLayout3 extends RelativeLayout {
    public MyRelativeLayout3(Context context) {
        super(context);
    }

    public MyRelativeLayout3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
