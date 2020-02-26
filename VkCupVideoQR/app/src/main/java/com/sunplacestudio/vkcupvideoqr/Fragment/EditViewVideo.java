package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.sunplacestudio.vkcupvideoqr.R;

import java.lang.ref.WeakReference;

import static android.support.v4.content.ContextCompat.getDrawable;

public class EditViewVideo extends View {

    private Paint paint;

    private Bitmap bitmapLeftTrim;
    private Bitmap bitmapLeftBackground;

    private Bitmap bitmapRightTrim;
    private Bitmap bitmapMiddle;
    private Drawable bitmapBackGround;

    private int height;
    private int left = 1;
    private RectF rect = new RectF(0, 0, left, 0);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rect, paint);
        bitmapBackGround.draw(canvas);
    }

    public EditViewVideo(Context context) {
        super(context);
        init();
    }

    public EditViewVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditViewVideo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#AA818C99"));
        // bitmapBackGround = BitmapFactory.decodeResource(getResources(), R.drawable.background_hide_part_video);
        bitmapBackGround  = getDrawable(getContext(), R.drawable.video_edit_background);
        bitmapLeftTrim = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_trim_arrow_left);
        bitmapLeftBackground = BitmapFactory.decodeResource(getResources(), R.drawable.button_background_left);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        height = h;
        rect = new RectF(0, 0, left, height);
        bitmapBackGround.setBounds(0, 0, w, h);
        /*Matrix matrix = new Matrix();
        bitmapBackGround = BitmapFactory.decodeResource(getResources(), R.drawable.background_hide_part_video);
        int scaleX = bitmapBackGround.getWidth() / w;
        int scaleY = bitmapBackGround.getHeight() / h;
        matrix.setScale(scaleX, scaleY);
        bitmapLeftBackground = Bitmap.createBitmap(bitmapBackGround, 0, 0, bitmapBackGround.getWidth(), bitmapBackGround.getHeight(), matrix, true);*/

        invalidate();
    }

    public void setLeftDraw(int left) {
        rect = new RectF(2, 0, left, height);
        invalidate();
    }
}
