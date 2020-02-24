package com.sunplacestudio.vkcupvideoqr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.MainActivity.LOG_TAG;

public class CameraService {
    private String cameraId;
    private CameraDevice cameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private CameraManager cameraManager;
    private TextureView textureView;
    private int type;
    private Display display;

    public static final int FRONT = 0;
    public static final int BACK = 1;

    private int orgPreviewWidth;
    private int orgPreviewHeight;

    public CameraService(String cameraID, TextureView textureView, int type, Activity activity) {
        this.cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);;
        this.cameraId = cameraID;
        this.textureView = textureView;
        this.type = type;

        try {
            StreamConfigurationMap configurationMap = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);

            orgPreviewWidth = sizesJPEG[0].getWidth();
            orgPreviewHeight = sizesJPEG[1].getHeight();
        } catch (CameraAccessException ex) {
            //
        }

        display = activity.getWindowManager().getDefaultDisplay();
    }

    public int getType() { return type; }

    public boolean isOpen() {
        return cameraDevice != null;
    }

    public void openCamera(Context context, int width, int height) {
        try {
            if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            else
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            textureView.setSurfaceTextureListener(surfaceTextureListener);
            transformImage(orgPreviewWidth, orgPreviewHeight);
            // updateTextureMatrix(orgPreviewWidth, orgPreviewHeight);
        } catch (CameraAccessException ex) {
            //
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            transformImage(width, height);
            // updateTextureMatrix(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice.StateCallback stateCallbackCamera = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };


    public void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void createCameraPreviewSession() {

        SurfaceTexture texture = textureView.getSurfaceTexture();
        // texture.setDefaultBufferSize(1920,1080);
         Surface surface = new Surface(texture);

        try {
            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {
                        mCaptureSession.setRepeatingRequest(builder.build(),null,null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) { }}, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateTextureMatrix(int width, int height) {
        /*boolean isPortrait = false;

        isPortrait = display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180;
        // if (display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180) isPortrait = true;
        // else if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) isPortrait = false;

        int previewWidth = orgPreviewWidth;
        int previewHeight = orgPreviewHeight;

        if (isPortrait)
        {
            previewWidth = orgPreviewHeight;
            previewHeight = orgPreviewWidth;
        }

        float ratioSurface = (float) width / height;
        float ratioPreview = (float) previewWidth / previewHeight;

        float scaleX;
        float scaleY;

        if (ratioSurface > ratioPreview)
        {
            scaleX = (float) height / previewHeight;
            scaleY = 1;
        }
        else
        {
            scaleX = 1;
            scaleY = (float) width / previewWidth;
        }*/

        Matrix matrix = new Matrix();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        // float scaleY = (float) textureView.getHeight() / (float) height;
        // float scaleX = (float) textureView.getWidth() / (float) width;

        // LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams()
        // textureView.setLayoutParams();

        float scaleY = (float) height / (float) textureView.getHeight(); // not work this variant
        float scaleX = (float) width / (float) textureView.getWidth();

        // float scaleY = (float) height / (float) displayMetrics.heightPixels;
        // float scaleX = (float) width / (float) displayMetrics.widthPixels;

        matrix.setScale(scaleX, scaleY);
        textureView.setTransform(matrix);

        /*float scaledWidth = width * scaleX;
        float scaledHeight = height * scaleY;

        float dx = (width - scaledWidth) / 2;
        float dy = (height - scaledHeight) / 2;
        textureView.setTranslationX(dx);
        textureView.setTranslationY(dy);*/
    }

    private void transformImage(int width, int height)
    {
        try {
            Matrix matrix = new Matrix();
            int rotation = display.getRotation();
            RectF textureRectF = new RectF(0, 0, width, height);
            RectF previewRectF = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
            float centerX = textureRectF.centerX();
            float centerY = textureRectF.centerY();
            if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
                matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
                float scale = Math.max((float) width / width, (float) height / width);
                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            }
            textureView.setTransform(matrix);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Pair<Integer, Integer> getMaxSize() {
        int width = 0;
        int height = 0;
        try {
            StreamConfigurationMap configurationMap = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);

            for (Size item : sizesJPEG) {
                if (item.getWidth() * item.getHeight() > width * height) {
                    width = item.getWidth();
                    height = item.getHeight();
                }
            }

        } catch (CameraAccessException ex) {
            //
        }
        return new Pair<Integer, Integer>(width, height);
    }


}
