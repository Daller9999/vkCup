package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.VideoView;

import java.util.List;

public class VideoFrameViewer extends View {
    private Paint paintMain;
    private List<Bitmap> bitmapList;
    private int width;
    private int height;

    public VideoFrameViewer(Context context) {
        super(context);
        init();
    }

    public VideoFrameViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoFrameViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setBitmaps(List<Bitmap> bitmapList) {
        this.bitmapList = bitmapList;
        invalidate();
    }

    private void init() {
        paintMain = new Paint();
        paintMain.setColor(Color.parseColor("#1a001C3D"));
    }

}
