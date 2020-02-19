package com.example.vkcupalbums.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vkcupalbums.AlbumInfo;
import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.R;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhotoAlbum;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class FragmentAlbums extends Fragment {

    private int userId;
    private AlbumInfo[] albumInfos;

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapter = new RecyclerAdapter(getContext());
        recyclerView.setAdapter(recyclerAdapter);

        userId = Integer.valueOf(VKAccessToken.currentToken().userId);

        loadUserAlbums(); //3F8AE0

        return view;
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

                    VKApiPhotoAlbum vkApiPhotoAlbum;
                    VKApiPhotoAlbum[] vkApiPhotoAlbums = new VKApiPhotoAlbum[count];
                    for (int i = 0; i < count; i++) {
                        vkApiPhotoAlbum = new VKApiPhotoAlbum();
                        vkApiPhotoAlbums[i] = vkApiPhotoAlbum.parse((JSONObject) data.get(i));
                    }
                    new ThreadLoadData(vkApiPhotoAlbums).start();
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

    private class ThreadLoadData extends Thread {
        private VKApiPhotoAlbum[] vkApiPhotoAlbums;

        ThreadLoadData(VKApiPhotoAlbum[] vkApiPhotoAlbums) {
            this.vkApiPhotoAlbums = vkApiPhotoAlbums;
        }

        @Override public void run() {
            AlbumInfo[] albumInfos = new AlbumInfo[2];
            for (int i = 0; i < vkApiPhotoAlbums.length; i++) {
                VKApiPhotoAlbum vkApiPhotoAlbum = vkApiPhotoAlbums[i];
                String http = vkApiPhotoAlbum.thumb_src;

                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(http).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("mesUri", "error to load image : " + e.getMessage());
                }

                int pos = i % 2 == 1 ? 0 : 1;
                albumInfos[pos] = new AlbumInfo(vkApiPhotoAlbum.title, vkApiPhotoAlbum.id, mIcon11, vkApiPhotoAlbum.size);

                if (i % 2 == 1) {
                    final AlbumInfo[] albumInfos1 = albumInfos;
                    getActivity().runOnUiThread(() -> recyclerAdapter.addAlbumInfo(albumInfos1));
                    albumInfos = new AlbumInfo[2];
                }
            }
        }
    }
}
