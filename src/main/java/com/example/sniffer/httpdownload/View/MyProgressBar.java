package com.example.sniffer.httpdownload.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * 自定义ProgressBar
 */
public class MyProgressBar extends ProgressBar {
    private String text_progress;
    private Paint mPaint;//画笔

    public MyProgressBar(Context context) {
        super(context);
        initPaint();
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        setTextProgress(progress);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        mPaint.getTextBounds(text_progress, 0, text_progress.length(), rect);
        mPaint.setTextSize(20);
        mPaint.setFakeBoldText(true);
        // 让显示的字体处于中心位置;
        int x = (getWidth() / 2) - rect.centerX();
        int y = (getHeight() / 2) - rect.centerY();
        //显示文字
        canvas.drawText(text_progress, x, y, mPaint);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        // 设置抗锯齿;
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
    }

    /**
     * 设置文字内容
     * @param progress
     */
    public void setTextProgress(int progress) {
        int i = (int) ((progress * 1.0f / getMax()) * 100);
        text_progress = String.valueOf(i) + "%";
    }
}
