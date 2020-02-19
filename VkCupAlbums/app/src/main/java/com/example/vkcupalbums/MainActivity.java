package com.example.vkcupalbums;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupalbums.Fragments.FragmentAlbums;
import com.example.vkcupalbums.Fragments.FragmentPhotoAlbum;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiPhotos;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPhotoAlbum;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VkAudioArray;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] otherPermissions = {VKScope.PHOTOS};
        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, otherPermissions);
        else {
            key = VKAccessToken.currentToken().accessToken;
            userId = Integer.valueOf(VKAccessToken.currentToken().userId);
            loadFragmentAlbums();
        }
        Log.e("mesUri", "api is : " + VKSdk.getApiVersion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override
            public void onResult(VKAccessToken res) {
                key = res.accessToken;
                userId = Integer.valueOf(res.userId);
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
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentAlbums()).commit();
    }

    public void loadFragmentPhoto(int id) {
        FragmentPhotoAlbum fragmentPhotoAlbum = new FragmentPhotoAlbum();
        fragmentPhotoAlbum.setAlbumId(id);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentPhotoAlbum).commit();
    }

}
