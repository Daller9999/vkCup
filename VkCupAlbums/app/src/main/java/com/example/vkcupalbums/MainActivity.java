package com.example.vkcupalbums;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private String key;
    private AlbumInfo[] albumInfos;

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;

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
            getUserAlbums();
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                key = res.accessToken;
                getUserAlbums();
                // Пользователь успешно авторизовался
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadTokens() {
        VKRequest vkRequest = new VKRequest("account.getAppPermissions", VKParameters.from(VKApiConst.USER_ID, userId));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
    }

    private void getUserAlbums() {
        VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList<VKApiUserFull> vkApiUserFulls = (VKList<VKApiUserFull>) response.parsedModel;
                VKApiUserFull vkApiUserFull = (VKApiUserFull) vkApiUserFulls.toArray()[0];
                userId = vkApiUserFull.id;
                loadUserAlbums();
                // loadGroups(userId);
            }
        });
    }

    private void loadUserPhotoAlbum() {
        VKRequest vkRequest = new VKRequest("photos.get", VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.ALBUM_ID, "profile", VKApiConst.COUNT, 1000));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
        // VKApi.photos().
    }

    private void loadUserAlbums() {
        VKRequest vkRequest = new VKRequest("photos.getAlbums", VKParameters.from(VKApiConst.OWNER_ID, userId, "need_system", 1, "need_covers", 1));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject jsonObject = response.json;
                try {
                    JSONObject jsonObject1 = ((JSONObject) jsonObject.get("response"));
                    int count = jsonObject1.getInt("count");
                    JSONArray data = jsonObject1.getJSONArray("items");

                    albumInfos = new AlbumInfo[count];
                    List<AlbumInfo> albumInfos = new ArrayList<>();
                    VKApiPhotoAlbum vkApiPhotoAlbum = new VKApiPhotoAlbum();

                    int j = 0;
                    for (int i = 0; i < count; i += 2) {
                        vkApiPhotoAlbum = vkApiPhotoAlbum.parse((JSONObject) data.get(i));

                        albumInfos.add(new AlbumInfo(vkApiPhotoAlbum.title, vkApiPhotoAlbum.id, vkApiPhotoAlbum.thumb_src, vkApiPhotoAlbum.size, null));
                        if (i % 2 == 0)
                            j++;
                    }
                    recyclerAdapter = new RecyclerAdapter(getApplicationContext(), albumInfos);
                    recyclerView.setAdapter(recyclerAdapter);
                } catch (JSONException ex) {
                    Log.e("mesUri", "json error : " + ex.getMessage());
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
        // VKApi.photos().
    }

    private class ThreadLoad extends Thread implements AlbumInfo.OnPhotoLoad {
        private VKApiPhotoAlbum[] vkApiPhotoAlbums;
        private volatile boolean wait = true;

        ThreadLoad(VKApiPhotoAlbum[] vkApiPhotoAlbums) {
            this.vkApiPhotoAlbums = vkApiPhotoAlbums;
        }

        @Override public void run() {
            try {
                VKApiPhotoAlbum vkApiPhotoAlbum = vkApiPhotoAlbums[0];
                AlbumInfo albumInfo1 = new AlbumInfo(vkApiPhotoAlbum.title, vkApiPhotoAlbum.id, vkApiPhotoAlbum.thumb_src, vkApiPhotoAlbum.size, this);
                while (wait)
                    sleep(50);

                wait = true;
                VKApiPhotoAlbum vkApiPhotoAlbum2 = vkApiPhotoAlbums[0];
                AlbumInfo albumInfo2 = new AlbumInfo(vkApiPhotoAlbum2.title, vkApiPhotoAlbum2.id, vkApiPhotoAlbum2.thumb_src, vkApiPhotoAlbum2.size, this);
                while (wait)
                    sleep(50);

                AlbumInfo[] albumInfos = {albumInfo1, albumInfo2};
                runOnUiThread(() -> recyclerAdapter.addAlbumInfo(albumInfos));
            } catch (InterruptedException ex) {
                //
            }
        }

        @Override public void onLoad() {
            wait = false;
        }
    }
}
