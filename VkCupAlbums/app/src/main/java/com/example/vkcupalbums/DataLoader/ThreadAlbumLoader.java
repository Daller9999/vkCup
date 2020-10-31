package com.example.vkcupalbums.DataLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.vkcupalbums.Objects.AlbumInfo;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhotoAlbum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import static java.lang.Thread.sleep;

public class ThreadAlbumLoader extends ThreadRunners {
    private int offset = 0;
    private int count = 50;
    private boolean wait = true;
    private OnAlbumsLoad onAlbumsLoad;
    private List<AlbumInfo> albumInfoList = new ArrayList<>();

    ThreadAlbumLoader(OnAlbumsLoad onAlbumsLoad, Handler handler, OnOverLoad onOverLoad) {
        super(onOverLoad, handler);
        this.onAlbumsLoad = onAlbumsLoad;
    }

    @Override public void run() {

        while (count > 0) {
            VKRequest vkRequest = new VKRequest("photos.getAlbums",
                    VKParameters.from(VKApiConst.OWNER_ID, Integer.valueOf(VKAccessToken.currentToken().userId), "need_system", 1, "need_covers", 1, "offset", offset, "count", count));
            vkRequest.executeWithListener(vkRequestListener);
            try {
                while (wait)
                    sleep(50);
            } catch (InterruptedException ex) {
                //
            }
        }

        for (int i = 0; i < albumInfoList.size(); i++) {
            String http = albumInfoList.get(i).getHttp();

            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(http).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("mesUri", "error to load image : " + e.getMessage());
            }

            albumInfoList.get(i).setBitmapMain(mIcon11);
        }

        handler.post(() -> onAlbumsLoad.onLoad(albumInfoList));
        onOverLoad.onOverLoad();
    }

    private VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            JSONObject jsonObject = response.json;
            try {
                JSONObject jsonObject1 = ((JSONObject) jsonObject.get("response"));
                count = jsonObject1.getInt("count");
                JSONArray data = jsonObject1.getJSONArray("items");

                VKApiPhotoAlbum vkApiPhotoAlbum;
                VKApiPhotoAlbum[] vkApiPhotoAlbums = new VKApiPhotoAlbum[count];
                for (int i = 0; i < count; i++) {
                    vkApiPhotoAlbum = new VKApiPhotoAlbum();
                    vkApiPhotoAlbums[i] = vkApiPhotoAlbum.parse((JSONObject) data.get(i));
                }
                for (VKApiPhotoAlbum vkApiPhotoAlbum1 : vkApiPhotoAlbums)
                    albumInfoList.add(new AlbumInfo(vkApiPhotoAlbum1.title, vkApiPhotoAlbum1.id, vkApiPhotoAlbum1.size, vkApiPhotoAlbum1.thumb_src));

                offset += count;
                wait = false;
            } catch (JSONException ex) {
                Log.e("mesUri", "json error : " + ex.getMessage());
            }
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
        }
    };

    public interface OnAlbumsLoad {
        void onLoad(List<AlbumInfo> albumInfoList);
    }
}


