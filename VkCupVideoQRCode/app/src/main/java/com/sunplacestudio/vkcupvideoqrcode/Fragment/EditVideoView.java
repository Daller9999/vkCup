package com.sunplacestudio.vkcupvideoqrcode.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.sunplacestudio.vkcupvideoqrcode.R;

import static android.support.v7.content.res.AppCompatResources.getDrawable;
import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.getDp;


public class EditVideoView extends View implements View.OnTouchListener {

    private Paint paint;
    private Paint paintTrim;
    private Paint paintRect;

    private Bitmap bitmapLeftTrim;
    private Drawable bitmapLeftBackground;
    private int frameWidth;

    private Bitmap bitmapRightTrim;
    private Drawable bitmapRightDrawable;
    private Drawable bitmapBackGround;

    private CustomImageView customImageView;

    private boolean trimLeft = false;
    private boolean trimRight = false;
    private int height;
    private int width;
    private int left = 0;
    private int right = 0;
    private int rectHeight;
    private RectF rectLeft = new RectF(0, 0, left, 0);
    private RectF rectRight = new RectF(0, 0, 0, 0);
    private RectF rectUp = new RectF(0, 0, 0, 0);
    private RectF rectDown = new RectF(0, 0, 0, 0);

    private OnPercentSwipeListener onPercentSwipeListener;

    public void setOnPercentSwipeListener(OnPercentSwipeListener onPercentSwipeListener) { this.onPercentSwipeListener = onPercentSwipeListener; }

    public void setCustomImageView(CustomImageView customImageView) {
        this.customImageView = customImageView;
    }

    private CustomImageView.OnMoveListener onMoveListener = (x) -> {
        if (onPercentSwipeListener != null)
            onPercentSwipeListener.onMiddle((int) ((float) x / (float) width * 100f));
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rectLeft, paint);
        canvas.drawRect(rectRight, paint);
        bitmapBackGround.draw(canvas);

        bitmapLeftBackground.draw(canvas);
        canvas.drawBitmap(bitmapLeftTrim, left + frameWidth / 8, height / 3, paintTrim);

        bitmapRightDrawable.draw(canvas);
        canvas.drawBitmap(bitmapRightTrim, right - frameWidth + frameWidth / 8, height / 3, paintTrim);

        canvas.drawRect(rectUp, paintRect);
        canvas.drawRect(rectDown, paintRect);
    }

    public EditVideoView(Context context) {
        super(context);
        init();
    }

    public EditVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#AA818C99"));

        paintTrim = new Paint();
        paintTrim.setColor(Color.WHITE);

        paintRect = new Paint();
        paintRect.setColor(Color.BLACK);

        bitmapBackGround  = getDrawable(getContext(), R.drawable.video_edit_background);

        bitmapLeftTrim = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_trim_arrow_left);
        bitmapLeftBackground = getDrawable(getContext(), R.drawable.button_background_left);

        bitmapRightTrim = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_trim_arrow_right);
        bitmapRightDrawable = getDrawable(getContext(), R.drawable.button_background_right);

        setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        right = w;
        height = h;
        rectLeft = new RectF(0, 0, left, height);
        bitmapBackGround.setBounds(0, 0, w, h);
        frameWidth = getDp(getContext(), 12);
        bitmapLeftBackground.setBounds(0, 0, frameWidth, h);
        bitmapRightDrawable.setBounds(right - frameWidth, 0, right, h);
        rectHeight = getDp(getContext(), 5);

        Matrix matrix = new Matrix();
        /*float scaleX = w / 2f / bitmapLeftTrim.getWidth();
        float scaleY = h / (float) getDp(getContext(), 30);
        matrix.setScale(scaleX, scaleY);*/
        matrix.setScale(1, 1);
        bitmapLeftTrim = Bitmap.createBitmap(bitmapLeftTrim, 0, 0, bitmapLeftTrim.getWidth(), bitmapLeftTrim.getHeight(), matrix, true);

        matrix = new Matrix();
        matrix.setScale(1, 1);
        bitmapRightTrim = Bitmap.createBitmap(bitmapRightTrim, 0, 0, bitmapRightTrim.getWidth(), bitmapRightTrim.getHeight(), matrix, true);

        customImageView.setOnMoveListener(onMoveListener);
        customImageView.setStartEndX(getX(), getX() + width);

        setRectUpDown();
    }

    private void setRectUpDown() {
        rectUp = new RectF(left + frameWidth, 0, right - frameWidth, rectHeight);
        rectDown = new RectF(left + frameWidth, height, right - frameWidth, height - rectHeight);
    }

    private void setLeftDraw(int left) {
        rectLeft = new RectF(10, 0, left + frameWidth, height);
        bitmapLeftBackground.setBounds(left, 0, left + frameWidth, height);
        this.left = left;
        setRectUpDown();
        invalidate();
    }

    private void setRightDraw(int right) {
        rectRight = new RectF(right - frameWidth, 0, width - 10, height);
        bitmapRightDrawable.setBounds(right - frameWidth, 0, right, height);
        this.right = right;
        setRectUpDown();
        invalidate();
    }

    @Override public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            trimLeft = false;
            trimRight = false;
            if (left - frameWidth * 2 <= x && x <= left + frameWidth * 2)
                trimLeft = true;
            else if (right - frameWidth * 2 <= x && x <= right + frameWidth * 2)
                trimRight = true;
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int x = (int) event.getX();
            if (trimLeft && x >= 0 && Math.abs(right - x) >= frameWidth * 5) {
                setLeftDraw(x);
                if (onPercentSwipeListener != null)
                    onPercentSwipeListener.onLeft((int) ((float) x / (float) width * 100f));
            } else if (trimRight && x <= width && Math.abs(left - x) >= frameWidth * 5) {
                setRightDraw(x);
                if (onPercentSwipeListener != null)
                    onPercentSwipeListener.onRight((int) ((float) x / (float) width * 100f));
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            trimLeft = false;
            trimRight = false;
            invalidate();
        }
        return true;
    }

    public interface OnPercentSwipeListener {
        void onLeft(int percent);
        void onRight(int percent);
        void onMiddle(int percent);
    }
}
