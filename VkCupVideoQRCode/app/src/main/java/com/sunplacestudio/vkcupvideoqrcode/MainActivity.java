package com.sunplacestudio.vkcupvideoqrcode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.sunplacestudio.vkcupvideoqrcode.Fragment.FragmentCamera;
import com.sunplacestudio.vkcupvideoqrcode.Fragment.FragmentEdit;
import com.sunplacestudio.vkcupvideoqrcode.Fragment.FragmentPermissions;

import java.io.File;



public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "mesUri";
    private boolean isLoad = false;

    private static final int FRAGMENT_CAMERA = 0;
    private static final int FRAGMENT_EDIT_VIDEO = 1;
    private int currentFragment = 0;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
        };
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
            for (String permissoin : permissions)
                if (ContextCompat.checkSelfPermission(this, permissoin) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(new String[]{permissoin}, 1);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void loadCameraFragment() {
        currentFragment = FRAGMENT_CAMERA;
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentCamera()).commit();
    }

    public void showEditVideoFragment(File file) {
        currentFragment = FRAGMENT_EDIT_VIDEO;
        FragmentEdit fragmentEdit = new FragmentEdit();
        fragmentEdit.setFileEdit(file);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentEdit).commit();
    }

    public static int getDp(Context context, int len) { return (int) (len * context.getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        if (currentFragment == FRAGMENT_EDIT_VIDEO)
            loadCameraFragment();
        else if (currentFragment == FRAGMENT_CAMERA) {
            moveTaskToBack(true);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean ok = true;
        for (int i = 0; i < permissions.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                ok = false;
                break;
            }
        if (ok)
            ok = permissions.length != 0;

        if (ok)
            loadCameraFragment();
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentPermissions()).commit();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
