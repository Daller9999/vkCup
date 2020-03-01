package com.sunplacestudio.vkcupvideoqrcode.CustomComponents;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.TextureView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class CustomTextureView extends TextureView {

    private int mRatioWidth = 0;
    private boolean run = true;
    private int mRatioHeight = 0;

    public CustomTextureView(Context context) {
        this(context, null);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(int width, int height) {
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight)
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            else
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
        }
    }

    // Из-за этой части кода возникает утечка памяти, я чётно пытался исправить её, но всё было бестолку, именно в этом блоке кода и происхожит распознавание QR кода
    public void startTextureRecognize(OnTextureQrRecongnize onTextureQrRecongnize) {
        new Thread(() -> {
            long start = System.currentTimeMillis();
            int[] intArray;
            Reader reader;
            Result result;
            LuminanceSource source;
            BinaryBitmap bitmapBinary;
            Bitmap bitmap;
            while (run) {
                if (System.currentTimeMillis() - start > 1000) {
                    bitmap = getBitmap();
                    try {
                        if (bitmap != null) {
                            intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

                            source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                            bitmapBinary = new BinaryBitmap(new HybridBinarizer(source));

                            reader = new MultiFormatReader();
                            result = reader.decode(bitmapBinary);
                            String contents = result.getText();
                            onTextureQrRecongnize.onRecognize(contents);
                        }
                    } catch (Exception ex) {
                        onTextureQrRecongnize.onRecognize(null);
                    }
                    start = System.currentTimeMillis();
                }
            }

        }).start();
    }

    public void stopRecognize() {
        run = false;
    }

    public interface OnTextureQrRecongnize {
        void onRecognize(String text);
    }

}
