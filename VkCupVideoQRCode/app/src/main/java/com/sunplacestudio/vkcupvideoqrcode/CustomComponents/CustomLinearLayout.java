package com.sunplacestudio.vkcupvideoqrcode.CustomComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CustomLinearLayout extends LinearLayout {

    private OnSizeListener onSizeListener;

    public void setOnSizeListener(OnSizeListener onSizeListener) {
        this.onSizeListener = onSizeListener;
    }

    public CustomLinearLayout(Context context) {
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onSizeListener != null)
            onSizeListener.onSizeChanged(w, h);
    }

    public interface OnSizeListener {
        void onSizeChanged(int width, int height);
    }
}
