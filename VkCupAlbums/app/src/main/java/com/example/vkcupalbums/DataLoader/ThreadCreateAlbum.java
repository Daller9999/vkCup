package com.example.vkcupalbums.DataLoader;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupalbums.Objects.AlbumInfo;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhotoAlbum;

import org.json.JSONException;
import org.json.JSONObject;

class ThreadCreateAlbum extends ThreadRunners {

    private OnAlbumCreated onAlbumCreated;
    private String name;

    ThreadCreateAlbum(String name, OnAlbumCreated onAlbumCreated, Handler handler, OnOverLoad onOverLoad) {
        super(onOverLoad, handler);
        this.name = name;
        this.onAlbumCreated = onAlbumCreated;
    }


    @Override public void run() {
        VKRequest vkRequest = new VKRequest("photos.createAlbum",
                VKParameters.from(
                        VKApiConst.OWNER_ID, Integer.valueOf(VKAccessToken.currentToken().userId),
                        "title", name,
                        "type", "all",
                        "users", 0));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject jsonObject = response.json;
                try {
                    JSONObject jsonObject1 = ((JSONObject) jsonObject.get("response"));
                    VKApiPhotoAlbum vkApiPhotoAlbum = new VKApiPhotoAlbum();
                    final VKApiPhotoAlbum vkApiPhotoAlbum1 = vkApiPhotoAlbum.parse(jsonObject1);
                    AlbumInfo albumInfo = new AlbumInfo(vkApiPhotoAlbum1.title, vkApiPhotoAlbum1.id, 0, null);
                    handler.post(() -> onAlbumCreated.onCreate(albumInfo));
                } catch (JSONException ex) {
                    Log.e("mesUri", "json error : " + ex.getMessage());
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                onOverLoad.onMessage("Произошла ошибка, не удалось создать группу, проверьте соединение с интернетом");
            }
        });

        onOverLoad.onOverLoad();
    }

    public interface OnAlbumCreated {
        void onCreate(AlbumInfo albumInfo);
    }

}
