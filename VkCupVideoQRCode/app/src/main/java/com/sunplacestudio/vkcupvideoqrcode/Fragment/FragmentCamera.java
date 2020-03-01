package com.sunplacestudio.vkcupvideoqrcode.Fragment;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.sunplacestudio.vkcupvideoqrcode.CustomComponents.CustomTextureView;
import com.sunplacestudio.vkcupvideoqrcode.CameraService;
import com.sunplacestudio.vkcupvideoqrcode.CustomComponents.ViewSqare;
import com.sunplacestudio.vkcupvideoqrcode.MainActivity;
import com.sunplacestudio.vkcupvideoqrcode.R;
import com.sunplacestudio.vkcupvideoqrcode.CustomComponents.RoundButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.LOG_TAG;

public class FragmentCamera extends Fragment {
    private List<CameraService> cameraIdsFront = new ArrayList<>();
    private List<CameraService> cameraIdsBack = new ArrayList<>();

    private CustomTextureView customTextureView;
    private boolean front = false;
    private boolean isMakeVideo = true;
    private int width;
    private ViewSqare viewFrame;
    private int height;

    private CameraService cameraServiceCurrent = null;

    private Animation animationIn;
    private Animation animationOut;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        viewFrame = view.findViewById(R.id.viewFrame);

        animationIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_qr_in);
        animationOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_qr_out);

        customTextureView = view.findViewById(R.id.videoView);
        customTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
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
                    cameraIdsFront.add(new CameraService(cameraID, customTextureView, onImageRecognizeListener, getActivity()));
                else if (typeFacing ==  CameraCharacteristics.LENS_FACING_BACK)
                    cameraIdsBack.add(new CameraService(cameraID, customTextureView, onImageRecognizeListener, getActivity()));
            }
        } catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        Button buttonSwitchCamera = view.findViewById(R.id.buttonSwichCamera);
        buttonSwitchCamera.setOnClickListener((v) -> {
            if (cameraIdsBack.isEmpty() || cameraIdsFront.isEmpty() || !isMakeVideo) return;
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
        buttonVideo.setOnLongTapRoundListener(new RoundButton.OnLongTapRoundListener() {
            @Override public void onLongTap(int percent) {
                if (cameraServiceCurrent != null)
                    cameraServiceCurrent.setZoom(percent);
                if (isMakeVideo)
                    setMakeVideo();
            }

            @Override public void onClick() {
                if (cameraServiceCurrent != null)
                    setMakeVideo();
            }
        });

        Button buttonFlash = view.findViewById(R.id.buttonFlash);
        buttonFlash.setOnClickListener((v) -> {
            buttonFlash.setBackgroundResource(cameraServiceCurrent.getFlashMode() ? R.mipmap.ic_flash_shadow_48 : R.mipmap.ic_flash_off_outline_shadow_48);
            cameraServiceCurrent.setLightMode();
        });

        return view;
    }

    private void setMakeVideo() {
        if (cameraServiceCurrent != null) {
            if (isMakeVideo) {
                isMakeVideo = false;
                cameraServiceCurrent.startRecordingVideo(getContext());
            } else {
                File file = cameraServiceCurrent.stopRecordingVideo();
                if (getActivity() != null)
                    ((MainActivity) getActivity()).showEditVideoFragment(file);
            }
        }
    }

    private long lastRecognize = 0;
    private CameraService.OnImageRecognizeListener onImageRecognizeListener = (text) -> {
        if (text == null && System.currentTimeMillis() - lastRecognize > 3000) {
            if (viewFrame.getVisibility() == View.VISIBLE) {
                viewFrame.startAnimation(animationOut);
                viewFrame.setVisibility(View.GONE);
            }
        } else if (text != null && System.currentTimeMillis() - lastRecognize > 2000) {
            Toast.makeText(getContext(), "QR код : " + text, Toast.LENGTH_SHORT).show();
            if (viewFrame.getVisibility() == View.GONE) {
                viewFrame.startAnimation(animationIn);
                viewFrame.setVisibility(View.VISIBLE);
            }
            lastRecognize = System.currentTimeMillis();
        }
    };
}
