package com.example.vkcupalbums;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupalbums.Fragments.Photo.FragmentAlbums;
import com.example.vkcupalbums.Fragments.Photo.FragmentPhoto;
import com.example.vkcupalbums.Fragments.Photo.FragmentPhotoAlbum;
import com.example.vkcupalbums.Objects.PhotoInfo;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class MainActivity extends AppCompatActivity {


    private int currentFragment;
    private int FRAGMENT_ALBUM = 0;
    private int FRAGMENT_PHOTOS = 1;
    private int FRAGMENT_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(permissions, 1);

        String[] otherPermissions = {VKScope.PHOTOS};
        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, otherPermissions);
        else {
            ViewPager viewPager = findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(2);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
        }
        Log.e("mesUri", "api is : " + VKSdk.getApiVersion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override
            public void onResult(VKAccessToken res) {
                loadFragmentAlbums();
                // Пользователь успешно авторизовался
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void loadFragmentAlbums() {
        currentFragment = FRAGMENT_ALBUM;
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentAlbums()).addToBackStack(FragmentAlbums.class.getName()).commit();
    }

    public void loadFragmentPhoto(int id) {
        currentFragment = FRAGMENT_PHOTOS;
        FragmentPhotoAlbum fragmentPhotoAlbum = new FragmentPhotoAlbum();
        fragmentPhotoAlbum.setAlbumId(id);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentPhotoAlbum).addToBackStack(FragmentPhotoAlbum.class.getName()).commit();
    }

    public void loadFragmentPhoto(PhotoInfo photoInfo) {
        currentFragment = FRAGMENT_PHOTO;
        FragmentPhoto fragmentPhoto = new FragmentPhoto();
        fragmentPhoto.setPhoto(photoInfo);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentPhoto).addToBackStack(FragmentPhoto.class.getName()).commit();
    }

    public void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }

    @Override public void onBackPressed() {
        if (currentFragment == FRAGMENT_PHOTO) {
            currentFragment = FRAGMENT_PHOTOS;
            popBackStack();
        } else if (currentFragment == FRAGMENT_PHOTOS) {
            currentFragment = FRAGMENT_ALBUM;
            popBackStack();
        } else if (currentFragment == FRAGMENT_ALBUM) {
            moveTaskToBack(true);
            finish();
        }

    }

}
