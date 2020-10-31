package com.example.vkcupalbums.DataLoader;

import android.os.Handler;
import android.util.Log;

import com.example.vkcupalbums.Fragments.Photo.FragmentPhotoAlbum;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

class ThreadLoadAlbumPhotos extends ThreadRunners {

    private int albumId;

    private int offset = 0;
    private int count = 1000;
    private boolean wait = false;

    private OnPhotoLoaded onPhotoLoaded;
    private List<VKApiPhoto> vkApiPhotos = new ArrayList<>();

    ThreadLoadAlbumPhotos(Handler handler, OnOverLoad onOverLoad, int albumId, OnPhotoLoaded onPhotoLoaded) {
        super(onOverLoad, handler);
        this.albumId = albumId;
        this.onPhotoLoaded = onPhotoLoaded;
    }

    @Override public void run() {
        while (count > 0) {
            VKRequest vkRequest = new VKRequest("photos.get",
                    VKParameters.from(VKApiConst.OWNER_ID, Integer.valueOf(VKAccessToken.currentToken().userId), VKApiConst.ALBUM_ID, albumId, VKApiConst.COUNT, 1000, "offset", offset));
            vkRequest.executeWithListener(vkRequestListener);
            wait = true;
            try {
                while (wait)
                    sleep(50);
            } catch (InterruptedException ex) {
                //
            }
        }
        handler.post(() -> onPhotoLoaded.onPhotoLoaded(vkApiPhotos));
        onOverLoad.onOverLoad();
    }

    private VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
        @Override public void onComplete(VKResponse response) {
            super.onComplete(response);
            JSONObject jsonObject = response.json;
            try {
                JSONObject jsonObject1 = ((JSONObject) jsonObject.get("response"));
                JSONArray data = jsonObject1.getJSONArray("items");
                count = data.length();

                VKApiPhoto vkApiPhoto;
                for (int i = 0; i < count; i++) {
                    vkApiPhoto = new VKApiPhoto();
                    vkApiPhotos.add(vkApiPhoto.parse((JSONObject) data.get(i)));
                }
                offset += 1000;
            } catch (JSONException ex) {
                Log.e("mesUri", "json error : " + ex.getMessage());
            }
            wait = false;
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
            wait = false;
        }
    };

    public interface OnPhotoLoaded {
        public void onPhotoLoaded(List<VKApiPhoto> vkApiPhotos);
    }
}
