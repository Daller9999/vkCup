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
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.CameraService.BACK;
import static com.sunplacestudio.vkcupvideoqr.CameraService.FRONT;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "mesUri";
    private CameraManager mCameraManager = null;
    private List<String> cameraIdsFront = new ArrayList<>();
    private List<String> cameraIdsBack = new ArrayList<>();
    private List<CameraService> cameraServices = new ArrayList<>();

    private TextureView texttureView;

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

        texttureView = findViewById(R.id.imageView);
        texttureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                for (CameraService cameraService : cameraServices)
                    if (cameraService.getType() == BACK) {
                        cameraService.openCamera(MainActivity.this, i, i1);
                        break;
                    }
            }

            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            // выводим информацию по камере
            for (String cameraID : cameraManager.getCameraIdList()) {

                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraID);
                int typeFacing = cc.get(CameraCharacteristics.LENS_FACING);

                int type = 0;
                if (typeFacing ==  CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraIdsFront.add(cameraID);
                    type = FRONT;
                }

                if (typeFacing ==  CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdsBack.add(cameraID);
                    type = BACK;
                }

                cameraServices.add(new CameraService(cameraID, texttureView, type, this));
                /*
                StreamConfigurationMap configurationMap = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);

                if (sizesJPEG != null) {
                    for (Size item:sizesJPEG) {
                        Log.i(LOG_TAG, "w:"+item.getWidth()+" h:"+item.getHeight());
                    }
                }  else {
                    Log.i(LOG_TAG, "camera don`t support JPEG");
                }*/
            }
        } catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException ex) {
            //
        }
    }

}
