package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomImageView extends AppCompatImageView implements View.OnTouchListener {

    private float dX;
    private float startX = -1;
    private float endX = 1;
    private OnMoveListener onMoveListener;

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public void setStartEndX(float startX, float endX) {
        this.startX = startX;
        this.endX = endX;
    }

    public CustomImageView(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
    }


    @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - motionEvent.getRawX();
                if (startX == -1)
                    startX = view.getX();
                // dY = view.getY() - motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = motionEvent.getRawX() + dX;
                if (x < startX)
                    return false;
                if (onMoveListener != null)
                    onMoveListener.onMove((int) x);
                view.animate()
                        .x(x)
                        // .y(motionEvent.getRawY() + dY)
                        .setDuration(0)
                        .start();
                break;
        }
        return true;
    }

    public interface OnMoveListener {
        void onMove(int x);
    }
}
