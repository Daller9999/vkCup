package com.sunplacestudio.vkcupvideoqr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.MainActivity.LOG_TAG;

public class CameraService {
    private String cameraId;
    private CameraDevice cameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private CameraManager cameraManager;
    private AutoFitTextureView textureView;
    private int type;
    private Display display;
    private MediaRecorder mediaRecorder = new MediaRecorder();

    public static final int FRONT = 0;
    public static final int BACK = 1;

    private Size mVideoSize;
    private Size mPreviewSize;

    private int orgPreviewWidth;
    private int orgPreviewHeight;

    public CameraService(String cameraID, AutoFitTextureView textureView, int type, Activity activity) {
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

    public void openCamera(Context context) {
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

    private static Size chooseVideoSize4and3(Size[] choices) {
        for (Size size : choices)
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080)
                return size;
        return choices[choices.length - 1];
    }

    private static Size chooseVideoSizeFullScreen(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == 1920 && size.getHeight() == 1080)
                return size;
        }
        return chooseVideoSize4and3(choices);
    }

    /*private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w && option.getWidth() >= width && option.getHeight() >= height)
                bigEnough.add(option);
        }
        return bigEnough.size() > 0 ? Collections.min(bigEnough, new CompareSizesByArea()) : choices[0];
    }*/

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        double ratio = (double) h / w;
        for (Size option : choices) {
            double optionRatio = (double) option.getHeight() / option.getWidth();
            if (ratio == optionRatio) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(LOG_TAG, "Couldn't find any suitable preview size");
            return choices[1];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    public void openCamera(Context context, int width, int height) {
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mVideoSize = chooseVideoSizeFullScreen(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, mVideoSize);

            textureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            configureTransform(width, height);
            mediaRecorder = new MediaRecorder();

            if (Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            else
                cameraManager.openCamera(cameraId, stateCallbackCamera, null);
            // cameraManager.openCamera(cameraId, stateCallbackCamera, null);
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

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {}
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            configureTransform(width, height);
            // transformImage(width, height);
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

        @Override public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };


    public void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void startPreview() {
        try {
            closePreviewSession();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                            } catch (CameraAccessException ex) {
                                //
                            }
                        }

                        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e("mesUri", "failed to start camera");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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

    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private void setUpMediaRecorder() throws IOException {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);


        File file = Environment.getExternalStorageDirectory();
        File files = new File(file.getPath() + "/" + "VkVideo");
        files.mkdir();
        String fullPath = files.getPath() + "/" + Calendar.getInstance().getTime().toString() + ".mp4";
        fullPath = fullPath.replace(" ", "_");
        fileOutput = new File(fullPath);

        mediaRecorder.setOutputFile(fullPath);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = display.getRotation();
        mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
        mediaRecorder.prepare();
    }

    private File fileOutput;

    private CaptureRequest.Builder mPreviewBuilder;
    public void startRecordingVideo() {
        try {
            closePreviewSession();
            setUpMediaRecorder();

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
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
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

    public File stopRecordingVideo() {

        mediaRecorder.stop();
        mediaRecorder.reset();

        return fileOutput;
    }

}
