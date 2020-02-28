package com.sunplacestudio.vkcupvideoqrcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RoundButton extends View {

    private Paint paintMain;
    private Paint paintSecond;

    private int radius;
    private int radius2;
    private int center;
    private int height;

    private float scaler = 0.8f;

    public RoundButton(Context context) {
        super(context);
        init();
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paintMain = new Paint();
        paintMain.setColor(Color.parseColor("#1a001C3D"));

        paintSecond = new Paint();
        paintSecond.setColor(Color.WHITE);
    }

    @Override public void onDraw(Canvas canvas) {
        canvas.drawCircle(center, center, radius, paintSecond);
        canvas.drawCircle(center, center, radius2, paintMain);
    }


    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        center = h / 2;
        radius = (int) (h / 2 * scaler);
        radius2 = (int) (radius * 0.8f);
        invalidate();
    }

    public void setPressedButton(boolean b) {
        scaler = b ? 1f : 0.8f;

        center = height / 2;
        radius = (int) (height / 2 * scaler);
        radius2 = (int) (radius * 0.8f);
        invalidate();
    }


    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
