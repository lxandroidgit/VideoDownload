package com.example.sniffer.httpdownload.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sniffer.httpdownload.R;

/**
 * 自定义指示器
 */
public class ViewPagerIndicator extends LinearLayout {
    private Paint mPaint; //画笔
    private int mTop; // 指示符的所在控件的Y
    private int mLeft; // 指示符的所在控件的X;
    private int mWidth; // 指示符的宽度
    private int mHeight = 8; // 指示符的高度
    private boolean position ;

    private TextView leftTextView;
    private TextView rightTextView;

    private String leftText;
    private int leftTextColor;
    private float leftTextSize;

    private String rightText;
    private int rightTextColor;
    private float rightTextSize;
    private onClickText listener;

    private LinearLayout.LayoutParams  layoutParams;
    private final String INSTANCE_STATUS = "instance_status";
    private final String STATUS_POSITION = "status_position";

    public interface onClickText {
        void onClickLeftText(boolean position);

        void onClickRightText(boolean position);
    }

    public void setOnClickTextListener(onClickText listener) {
        this.listener = listener;
    }

    public ViewPagerIndicator(Context context) {
        super(context);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
        //获取自定义属性值
        leftText = ta.getString(R.styleable.ViewPagerIndicator_leftText);
        leftTextColor = ta.getColor(R.styleable.ViewPagerIndicator_leftTextColor, 0);
        leftTextSize = ta.getDimension(R.styleable.ViewPagerIndicator_leftTextSize, 0);

        rightText = ta.getString(R.styleable.ViewPagerIndicator_rightText);
        rightTextColor = ta.getColor(R.styleable.ViewPagerIndicator_rightTextColor, 0);
        rightTextSize = ta.getDimension(R.styleable.ViewPagerIndicator_rightTextSize, 0);

        ta.recycle();
        //实例化控件
        leftTextView = new TextView(context);
        rightTextView = new TextView(context);
        //控件赋值
        leftTextView.setText(leftText);
        leftTextView.setTextColor(leftTextColor);
        leftTextView.setTextSize(leftTextSize);
        leftTextView.setGravity(Gravity.CENTER);

        rightTextView.setText(rightText);
        rightTextView.setTextColor(rightTextColor);
        rightTextView.setTextSize(rightTextSize);
        rightTextView.setGravity(Gravity.CENTER);

        setBackgroundColor(Color.WHITE);
        //给自定义控件设置布局并加载
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        addView(leftTextView, layoutParams);
        addView(rightTextView, layoutParams);

        leftTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                position = false;
                if (listener != null) {
                    listener.onClickLeftText(position);
                }
                isPosition();
                invalidate();
            }
        });

        rightTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                position = true;
                if (listener != null) {
                    listener.onClickRightText(position);
                }
                isPosition();
                invalidate();
            }

        });
        mPaint = new Paint();
        // 设置抗锯齿;
        mPaint.setAntiAlias(true);
        //设置画笔颜色
        mPaint.setColor(Color.BLUE);
        leftTextView.setTextColor(Color.BLUE);
    }

    public void isPosition() {
        if (position) {
            leftTextView.setTextColor(Color.BLACK);
            rightTextView.setTextColor(Color.BLUE);
            mLeft = rightTextView.getLeft();
            mWidth = rightTextView.getWidth();
        } else {
            rightTextView.setTextColor(Color.BLACK);
            leftTextView.setTextColor(Color.BLUE);
            mLeft = leftTextView.getLeft();
            mWidth = leftTextView.getWidth();
        }
    }

    /**
     * 测量控件
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTop = getMeasuredHeight();
        int width = getMeasuredWidth();
        int height = mTop + mHeight;
        isPosition();
        setMeasuredDimension(width, height);
    }

    /**
     * 绘制控件
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制矩形
        Rect rect = new Rect(mLeft, mTop, mLeft + mWidth, mTop + mHeight);
        canvas.drawRect(rect, mPaint);
    }

    /**
     * 回收前保存状态
     *
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
        bundle.putBoolean(STATUS_POSITION, position);
        return bundle;
    }

    /**
     * 重启后取出状态
     *
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            position = bundle.getBoolean(STATUS_POSITION);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
            Log.e(getClass() + "", "onRestoreInstanceState:" + position);
            return;
        }
        super.onRestoreInstanceState(state);
    }

}
