package com.sunplacestudio.vkcupvideoqrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.sunplacestudio.vkcupvideoqrcode.CustomComponents.CustomTextureView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.LOG_TAG;


public class CameraService {
    private String cameraId;
    private CameraDevice cameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private CameraManager cameraManager;
    private volatile CustomTextureView textureView;
    private Display display;
    private MediaRecorder mediaRecorder = new MediaRecorder();

    private Size mVideoSize;
    private Size mPreviewSize;

    private File fileOutput;
    private CaptureRequest.Builder mPreviewBuilder;
    private boolean flashMode = false;
    private OnImageRecognizeListener onImageRecognizeListener;
    private boolean qrRun = true;
    private float maxZoom = 0;

    private Rect rectZoom;

    public boolean getFlashMode() { return flashMode; }

    public CameraService(String cameraID, CustomTextureView textureView, OnImageRecognizeListener onImageRecognizeListener, Activity activity) {
        this.cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        this.cameraId = cameraID;
        this.textureView = textureView;
        this.onImageRecognizeListener = onImageRecognizeListener;
        display = activity.getWindowManager().getDefaultDisplay();
    }

    private static Size chooseVideoSizeFullScreen(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == 1920 && size.getHeight() == 1080)
                return size;
        }
        return choices[choices.length - 1];
    }


    private static Size chooseOptimalSize(Size[] choices, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        double ratio = (double) h / w;
        for (Size option : choices) {
            double optionRatio = (double) option.getHeight() / option.getWidth();
            if (ratio == optionRatio)
                bigEnough.add(option);
        }
        return bigEnough.size() > 0 ? Collections.min(bigEnough, new CompareSizesByArea()) : choices[1];
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public void openCamera(Context context, int width, int height) {
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mVideoSize = chooseVideoSizeFullScreen(map.getOutputSizes(MediaRecorder.class));
            // mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mVideoSize);
            mPreviewSize = mVideoSize;

            textureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            configureTransform(width, height);

            if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            else
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            qrRun = true;

            maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10;
        } catch (CameraAccessException e) {
            //
        } catch (NullPointerException e) {
            //
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        int rotation = display.getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private long lastFrame = 0;
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {}
        @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            configureTransform(width, height);
        }
        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) { return false; }
        @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
    };

    private CameraDevice.StateCallback stateCallbackCamera = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice cameraDevice, int i) {}
    };

    public void closeCamera() {
        if (cameraDevice != null) {
            qrRun = false;
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public String getCameraId() { return cameraId; }
    private Handler handler = new Handler();

    private void startPreview() {
        try {
            closePreviewSession();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                        @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                            } catch (CameraAccessException ex) {
                                Log.e(LOG_TAG, "error" + ex.getMessage());
                                ex.printStackTrace();
                            }
                            textureView.startTextureRecognize(onTextureQrRecongnize);
                        }

                        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e("mesUri", "failed to start camera");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CustomTextureView.OnTextureQrRecongnize onTextureQrRecongnize = (text) -> {
        handler.post(() -> postRecognize(text));
    };

    private Handler handlerPost = new Handler();

    private void postRecognize(String text) {
        if (onImageRecognizeListener != null)
            onImageRecognizeListener.onRecognize(text);
    }


    public void setZoom(int percent) {
        if (mCaptureSession == null) return;
        try {
            rectZoom = getZoomRect(maxZoom * percent / 100);
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, rectZoom);
            mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
            if (rectZoom != null)
                Log.i(LOG_TAG, "rectZoom : " + rectZoom.toString());
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    private Rect getZoomRect(float zoomLevel) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            float maxZoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if((zoomLevel <= maxZoom) && (zoomLevel > 1)) {
                int minW = (int) (activeRect.width() / maxZoom);
                int minH = (int) (activeRect.height() / maxZoom);
                int difW = activeRect.width() - minW;
                int difH = activeRect.height() - minH;
                int cropW = difW / 100 * (int) zoomLevel;
                int cropH = difH / 100 * (int) zoomLevel;
                cropW -= cropW & 3;
                cropH -= cropH & 3;
                return new Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH);
            } else if(zoomLevel == 0) {
                return new Rect(0, 0, activeRect.width(), activeRect.height());
            }
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error during camera init");
            return null;
        }
    }


    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private void setUpMediaRecorder(Context context) throws IOException {
        String fullPath = getVideoFilePath(context);
        fileOutput = new File(fullPath);

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setOutputFile(fullPath);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int rotation = display.getRotation();
        mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
        mediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/")) + "videoAt" + System.currentTimeMillis() + ".mp4";
    }

    public static String getTime() {
        return new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
    }


    public void startRecordingVideo(Context context) {
        try {
            qrRun = false;
            closePreviewSession();
            setUpMediaRecorder(context);

            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            Surface recorderSurface = mediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            if (rectZoom != null)
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, rectZoom);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCaptureSession = cameraCaptureSession;
                    try {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                    } catch (CameraAccessException ex) {
                        Log.e(LOG_TAG, "error to start to record video: " + ex.getMessage());
                    }
                    mediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {}
            }, null);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    public void setLightMode() {
        flashMode = !flashMode;
        mPreviewBuilder.set(CaptureRequest.FLASH_MODE, flashMode ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
        if (flashMode)
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    public File stopRecordingVideo() {
        closeCamera();

        textureView.stopRecognize();
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder = null;
        return fileOutput;
    }

    public interface OnImageRecognizeListener {
        void onRecognize(String text);
    }
}
