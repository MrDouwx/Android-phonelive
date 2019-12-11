package com.sevenshuyun.phonelive.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cxf on 2018/11/19.
 */

public class TestView extends View {

    private Paint mPaint;
    private float mScale;
    private RectF mRect;
    private int mWidth;
    private int mHeight;

    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScale = context.getResources().getDisplayMetrics().density;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xffff0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp2px(1));
        mRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int halfWidth = w / 2;
        int halfHeight = h / 2;
        mRect.left = halfWidth - halfHeight;
        mRect.right = halfWidth + halfHeight;
        mRect.top = halfHeight;
        mRect.bottom = halfHeight + h;
        mWidth = w;
        mHeight = h;
    }

    private int dp2px(float dpVal) {
        return (int) (mScale * dpVal + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Path path = new Path();
//        path.lineTo(0, 0);
//        path.moveTo(mWidth / 2, mHeight / 2);
//        path.arcTo(mRect, -90, 70);
//        canvas.drawPath(path, mPaint);


        Path path = new Path();
        path.lineTo(0, 0);
        path.moveTo(mWidth / 2, mHeight / 2);
        path.arcTo(mRect, -90, -70);
        canvas.drawPath(path, mPaint);
    }
}
