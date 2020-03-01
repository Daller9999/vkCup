
package com.example.vkcupfiles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vkcupfiles.Fragments.FragmentListFiles;
import com.example.vkcupfiles.Fragments.FragmentShowData;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class MainActivity extends AppCompatActivity {

    private int currentFragment;
    private int FRAGMENT_SHOW_FILE = 1;
    private int FRAGMENT_LIST_FILES = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
            for (String permissoin : permissions)
                if (ContextCompat.checkSelfPermission(this, permissoin) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(new String[]{permissoin}, 1);
        }

        String[] vkPermissions = new String[]{VKScope.DOCS};
        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(this, vkPermissions);
        } else {
            loadFragmentFilesList();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                loadFragmentFilesList();
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void loadFragmentFilesList() {
        currentFragment = FRAGMENT_LIST_FILES;
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentListFiles()).addToBackStack(FragmentListFiles.class.getName()).commit();
    }

    public void loadDataFile(VkDocsData vkDocsData) {
        currentFragment = FRAGMENT_SHOW_FILE;
        FragmentShowData fragmentShowData = new FragmentShowData();
        fragmentShowData.setData(vkDocsData);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out).replace(R.id.container, fragmentShowData)
                .addToBackStack(FragmentShowData.class.getName()).commit();
    }

    public void popBackStack() {
        currentFragment = FRAGMENT_LIST_FILES;
        getSupportFragmentManager().popBackStack();
    }

    @Override public void onBackPressed() {
        if (currentFragment == FRAGMENT_SHOW_FILE)
            popBackStack();
        else  {
            moveTaskToBack(true);
            finish();
        }

    }

}
