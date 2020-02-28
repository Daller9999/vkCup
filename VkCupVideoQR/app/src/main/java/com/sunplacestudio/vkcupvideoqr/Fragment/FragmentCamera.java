package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sunplacestudio.vkcupvideoqr.AutoFitTextureView;
import com.sunplacestudio.vkcupvideoqr.CameraService;
import com.sunplacestudio.vkcupvideoqr.MainActivity;
import com.sunplacestudio.vkcupvideoqr.R;
import com.sunplacestudio.vkcupvideoqr.RoundButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.MainActivity.LOG_TAG;

public class FragmentCamera extends Fragment {
    private CameraManager mCameraManager = null;
    private List<CameraService> cameraIdsFront = new ArrayList<>();
    private List<CameraService> cameraIdsBack = new ArrayList<>();

    private AutoFitTextureView autoFitTextureView;
    private boolean front = false;
    private boolean isMakeVideo = true;
    private int width;
    private int height;

    private CameraService cameraServiceCurrent = null;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        autoFitTextureView = view.findViewById(R.id.videoView);
        autoFitTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                width = i;
                height = i1;
                cameraServiceCurrent = cameraIdsBack.get(0);
                cameraServiceCurrent.openCamera(FragmentCamera.this.getContext(), width, height);
            }
            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) { return false; }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
        });

        CameraManager cameraManager = (CameraManager) view.getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : cameraManager.getCameraIdList()) {

                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                int typeFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

                if (typeFacing ==  CameraCharacteristics.LENS_FACING_FRONT)
                    cameraIdsFront.add(new CameraService(cameraID, autoFitTextureView, getActivity()));
                else if (typeFacing ==  CameraCharacteristics.LENS_FACING_BACK)
                    cameraIdsBack.add(new CameraService(cameraID, autoFitTextureView, getActivity()));

                /*StreamConfigurationMap configurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);
                if (sizesJPEG != null) {
                    String sT = typeFacing == CameraCharacteristics.LENS_FACING_BACK ? "BACK" : "FRONT";
                    Log.i(LOG_TAG, "camera id is : " + cameraID + " type is " + sT);
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

        Button buttonSwitchCamera = view.findViewById(R.id.buttonSwichCamera);
        buttonSwitchCamera.setOnClickListener((v) -> {
            if (cameraIdsBack.isEmpty() || cameraIdsFront.isEmpty()) return;
            front = !front;
            if (!front) {
                cameraIdsFront.get(0).closeCamera();
                cameraIdsBack.get(0).openCamera(getContext(), width, height);
                cameraServiceCurrent = cameraIdsBack.get(0);
            } else {
                cameraIdsBack.get(0).closeCamera();
                cameraIdsFront.get(0).openCamera(getContext(), width, height);
                cameraServiceCurrent = cameraIdsFront.get(0);
            }
        });

        RoundButton buttonVideo = view.findViewById(R.id.buttonVideo);
        buttonVideo.setOnClickListener((v) -> {
            if (cameraServiceCurrent != null) {
                if (isMakeVideo)
                    cameraServiceCurrent.startRecordingVideo(getContext());
                else {
                    File file = cameraServiceCurrent.stopRecordingVideo();
                    if (getActivity() != null)
                        ((MainActivity) getActivity()).showEditVideoFragment(file);
                }
                isMakeVideo = !isMakeVideo;
            }
        });

        Button buttonFlash = view.findViewById(R.id.buttonFlash);
        buttonFlash.setOnClickListener((v) -> {
            buttonFlash.setBackgroundResource(cameraServiceCurrent.getFlashMode() ? R.mipmap.ic_flash_shadow_48 : R.mipmap.ic_flash_off_outline_shadow_48);
            cameraServiceCurrent.setLightMode();
        });

        return view;
    }
}
