package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ViewSqare extends View {

    public ViewSqare(Context context) {
        this(context, null);
    }

    public ViewSqare(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewSqare(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
