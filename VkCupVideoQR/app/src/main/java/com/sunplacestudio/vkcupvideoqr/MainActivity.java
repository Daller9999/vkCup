package com.sunplacestudio.vkcupvideoqr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.CameraService.BACK;
import static com.sunplacestudio.vkcupvideoqr.CameraService.FRONT;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "mesUri";
    private CameraManager mCameraManager = null;
    private List<CameraService> cameraIdsFront = new ArrayList<>();
    private List<CameraService> cameraIdsBack = new ArrayList<>();

    private AutoFitTextureView autoFitTextureView;
    private boolean front = false;
    private boolean isMakeVideo = false;
    private int width;
    private int height;

    private CameraService cameraServiceCurrent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
            for (String permissoin : permissions)
                if (ContextCompat.checkSelfPermission(this, permissoin) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(new String[]{permissoin}, 1);
        }

        autoFitTextureView = findViewById(R.id.imageView);
        autoFitTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                width = i;
                height = i1;
                cameraServiceCurrent = cameraIdsBack.get(0);
                cameraServiceCurrent.openCamera(MainActivity.this, width, height);
            }
            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) { return false; }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
        });

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // выводим информацию по камере
            for (String cameraID : cameraManager.getCameraIdList()) {

                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraID);
                int typeFacing = cc.get(CameraCharacteristics.LENS_FACING);

                if (typeFacing ==  CameraCharacteristics.LENS_FACING_FRONT)
                    cameraIdsFront.add(new CameraService(cameraID, autoFitTextureView, FRONT, this));
                else if (typeFacing ==  CameraCharacteristics.LENS_FACING_BACK)
                    cameraIdsBack.add(new CameraService(cameraID, autoFitTextureView, BACK, this));
            }
        } catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException ex) {
            //
        }

        Button buttonSwitchCamera = findViewById(R.id.buttonSwichCamera);
        buttonSwitchCamera.setOnClickListener((v) -> {
            if (cameraIdsBack.isEmpty() || cameraIdsFront.isEmpty()) return;
            front = !front;
            if (!front) {
                cameraIdsFront.get(0).closeCamera();
                cameraIdsBack.get(0).openCamera(this, width, height);
                cameraServiceCurrent = cameraIdsBack.get(0);
            } else {
                cameraIdsBack.get(0).closeCamera();
                cameraIdsFront.get(0).openCamera(this, width, height);
                cameraServiceCurrent = cameraIdsFront.get(0);
            }
        });

        Button buttonVideo = findViewById(R.id.buttonVideo);
        buttonVideo.setOnClickListener((v) -> {
            if (cameraServiceCurrent != null) {
                isMakeVideo = !isMakeVideo;
                if (isMakeVideo)
                    cameraServiceCurrent.startRecordingVideo();
                else
                    cameraServiceCurrent.startRecordingVideo();
            }

        });
    }

}
