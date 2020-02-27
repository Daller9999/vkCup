package com.sunplacestudio.vkcupvideoqr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.sunplacestudio.vkcupvideoqr.Fragment.FragmentCamera;
import com.sunplacestudio.vkcupvideoqr.Fragment.FragmentEdit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.sunplacestudio.vkcupvideoqr.CameraService.BACK;
import static com.sunplacestudio.vkcupvideoqr.CameraService.FRONT;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "mesUri";

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // showEditVideoFragment(null);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentCamera()).addToBackStack(FragmentCamera.class.getName()).commit();

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }

    public void showEditVideoFragment(File file) {
        FragmentEdit fragmentEdit = new FragmentEdit();
        fragmentEdit.setFileEdit(file);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentEdit).addToBackStack(FragmentEdit.class.getName()).commit();
    }

    public static int getDp(Context context, int len) { return (int) (len * context.getResources().getDisplayMetrics().density + 0.5f); }

    public static final int flagsFullScreen = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // getWindow().getDecorView().setSystemUiVisibility(flagsFullScreen);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


}
