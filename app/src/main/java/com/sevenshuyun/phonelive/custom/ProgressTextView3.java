package com.sevenshuyun.phonelive.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sevenshuyun.phonelive.R;

/**
 * Created by cxf on 2017/9/2.
 * 带有环形进度条的TextView
 */

public class ProgressTextView3 extends TextView {

    public static final int MAX_PROGRESS = 100;
    private int mBgColor;
    private int mFgColor;
    private RectF mRectF;
    private int mRadius;
    private int mX;
    private Paint mPaint1;
    private Paint mPaint2;
    private int mProgress;

    public ProgressTextView3(Context context) {
        this(context, null);
    }

    public ProgressTextView3(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressTextView3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressTextView2);
        mBgColor = ta.getColor(R.styleable.ProgressTextView2_ptv2_bg_color, 0);
        mFgColor = ta.getColor(R.styleable.ProgressTextView2_ptv2_fg_color, 0);
        mProgress = ta.getInteger(R.styleable.ProgressTextView2_ptv2_progress, 0);
        ta.recycle();
        init();
    }

    private void init() {
        mPaint1 = new Paint();
        mPaint1.setAntiAlias(true);
        mPaint1.setDither(true);
        mPaint1.setStyle(Paint.Style.FILL);
        mPaint1.setColor(mBgColor);

        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setDither(true);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setColor(mFgColor);

        mRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mX = w / 2;
        mRadius = mX;
        mRectF.left = 0;
        mRectF.top = 0;
        mRectF.right = w;
        mRectF.bottom = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgress == 0) {
            canvas.drawCircle(mX, mX, mRadius, mPaint1);
        } else if (mProgress == MAX_PROGRESS) {
            canvas.drawCircle(mX, mX, mRadius, mPaint2);
        } else {
            canvas.drawCircle(mX, mX, mRadius, mPaint1);
            canvas.drawArc(mRectF, -90, 360 * mProgress / 100f, true, mPaint2);
        }
        super.onDraw(canvas);
    }


    public void setProgress(int progress) {
        if (progress > MAX_PROGRESS) {
            progress = MAX_PROGRESS;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        setText(String.valueOf(progress));
    }

}
