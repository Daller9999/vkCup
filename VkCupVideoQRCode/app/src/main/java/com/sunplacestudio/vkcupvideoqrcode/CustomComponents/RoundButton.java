package com.sunplacestudio.vkcupvideoqrcode.CustomComponents;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.os.Handler;

import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.LOG_TAG;
import static java.lang.Thread.sleep;

public class RoundButton extends View implements View.OnTouchListener {

    private Paint paintMain;
    private Paint paintSecond;
    private Paint paintZoom;

    private int radius;
    private int radius2;
    private int center;
    private int height;
    private float sweepAngle;

    private float scaler = 0.8f;
    private Handler handler = new Handler();
    private int percent = 0;
    private volatile boolean finish = true;
    private volatile boolean zoom = true;

    private OnLongTapRoundListener onLongTapRoundListener;

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
        paintMain.setColor(Color.parseColor("#CCCCCC"));

        paintZoom = new Paint();
        paintZoom.setColor(Color.parseColor("#ff3399"));

        paintSecond = new Paint();
        paintSecond.setColor(Color.WHITE);

        setOnTouchListener(this);
    }

    @Override public void onDraw(Canvas canvas) {
        canvas.drawCircle(center, center, radius, paintSecond);
        canvas.drawArc(0, 0, height, height, 270, sweepAngle, true, paintZoom);
        canvas.drawCircle(center, center, radius2, paintMain);
    }


    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        center = h / 2;
        radius = (int) (h / 2 * scaler);
        radius2 = (int) (radius * 0.8f);
        invalidate();
    }

    private void setPressedButton(boolean b) {
        scaler = b ? 1f : 0.8f;

        center = height / 2;
        radius = (int) (height / 2 * scaler);
        radius2 = (int) (radius * 0.8f);
        invalidate();
    }


    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }

    public void setOnLongTapRoundListener(OnLongTapRoundListener onLongTapRoundListener) { this.onLongTapRoundListener = onLongTapRoundListener; }

    private final int maxZoom = 80;

    @Override public boolean onTouch(View view, MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setPressedButton(true);
            if (finish)
                click();
        } else if (event.getAction() == MotionEvent.ACTION_UP)
            zoom = false;
        return true;
    }

    private void click() {
        finish = false;
        zoom = true;
        CountDownTimer countDownTimer = new CountDownTimer(1000, 500) {
            @Override public void onTick(long l) { }
            @Override public void onFinish() {
                if (zoom)
                    new Duration().start();
                else
                    onLongTapRoundListener.onClick();
                finish = true;
            }
        };
        countDownTimer.start();
    }

    private class Duration extends Thread {

        @Override public void run() {
            try {
                while (percent < maxZoom && zoom) {
                    percent++;
                    sweepAngle = 360 * percent / 100f;
                    sleep(250);
                    handler.post(() -> onLongTapRoundListener.onLongTap(percent));
                    postInvalidate();
                }
            } catch (InterruptedException ex) {
                //
            }
        }
    }

    public interface OnLongTapRoundListener {
        void onLongTap(int percent);
        void onClick();
    }
}
