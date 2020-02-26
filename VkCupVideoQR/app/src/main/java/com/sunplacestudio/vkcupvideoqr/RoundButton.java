package com.sunplacestudio.vkcupvideoqr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RoundButton extends View {

    private Paint paintMain;
    private Paint paintSecond;

    private int radius;
    private int radius2;

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
        canvas.drawCircle(radius, radius, radius, paintSecond);
        canvas.drawCircle(radius, radius, radius2, paintMain);
    }


    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = h / 2;
        radius2 = (int) (radius * 0.9f);
        invalidate();
    }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
