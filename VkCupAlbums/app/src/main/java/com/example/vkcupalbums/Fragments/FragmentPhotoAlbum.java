package com.example.vkcupalbums.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.vkcupalbums.AlbumInfo;
import com.example.vkcupalbums.PhotoInfo;
import com.example.vkcupalbums.R;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapter;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapterPhotos;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

// https://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk

import static android.app.Activity.RESULT_OK;

public class FragmentPhotoAlbum extends Fragment {

    private int albumId;
    private int userId;
    private RecyclerAdapterPhotos recyclerAdapterPhotos;
    private boolean edit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = Integer.valueOf(VKAccessToken.currentToken().userId);

        View view = inflater.inflate(R.layout.fragment_photo_albums, container, false);

        loadPhotoAlbum();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapterPhotos = new RecyclerAdapterPhotos(getContext());
        recyclerAdapterPhotos.setOnRecyclerClick(new RecyclerAdapterPhotos.OnRecyclerClick() {
            @Override public void onItemClick(int id) {

            }

            @Override public void onLongClick() {
                if (!edit) setEdit();
            }
        });
        recyclerView.setAdapter(recyclerAdapterPhotos);

        Button buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto);
        buttonAddPhoto.setOnClickListener((v) -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 111);
        });

        return view;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 111:
                if(resultCode == RESULT_OK){
                    VKApi.photos().getUploadServer(albumId).executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                        }
                    });
                    Uri selectedImage = data.getData();
                }
        }
    }

    private void setEdit() {
        edit = !edit;
        recyclerAdapterPhotos.setEdit(edit);
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    private void loadPhotoAlbum() {
        VKRequest vkRequest = new VKRequest("photos.get", VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.ALBUM_ID, albumId, VKApiConst.COUNT, 1000));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject jsonObject = response.json;
                try {
                    JSONObject jsonObject1 = ((JSONObject) jsonObject.get("response"));
                    int count = jsonObject1.getInt("count");
                    JSONArray data = jsonObject1.getJSONArray("items");

                    VKApiPhoto vkApiPhoto;
                    VKApiPhoto[] vkApiPhotos = new VKApiPhoto[count];
                    for (int i = 0; i < count; i++) {
                        vkApiPhoto = new VKApiPhoto();
                        vkApiPhotos[i] = vkApiPhoto.parse((JSONObject) data.get(i));
                    }
                    new ThreadLoadData(vkApiPhotos).start();
                } catch (JSONException ex) {
                    Log.e("mesUri", "json error : " + ex.getMessage());
                }
            }
        });
    }

    private class ThreadLoadData extends Thread {
        private VKApiPhoto[] vkApiPhotos;

        ThreadLoadData(VKApiPhoto[] vkApiPhotos) {
            this.vkApiPhotos = vkApiPhotos;
        }

        @Override public void run() {
            PhotoInfo[] photoInfos = new PhotoInfo[3];
            int pos = 0;
            for (int i = 0; i < vkApiPhotos.length; i++) {
                VKApiPhoto vkApiPhoto = vkApiPhotos[i];
                String[] photos = new String[]{vkApiPhoto.photo_2560, vkApiPhoto.photo_1280,
                                               vkApiPhoto.photo_807, vkApiPhoto.photo_604,
                                               vkApiPhoto.photo_130, vkApiPhoto.photo_75};
                String http = photos[0];
                for (String photoHttp : photos)
                    if (!photoHttp.isEmpty()) {
                        http = photoHttp;
                        break;
                    }

                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(http).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("mesUri", "error to load image : " + e.getMessage());
                }

                photoInfos[pos] = new PhotoInfo(mIcon11, vkApiPhoto.id);
                pos++;

                if (pos == 3 || i + 1 == vkApiPhotos.length) {
                    final PhotoInfo[] photoInfos1 = photoInfos;
                    getActivity().runOnUiThread(() -> recyclerAdapterPhotos.addPhotoInfo(photoInfos1));
                    photoInfos = new PhotoInfo[3];
                    pos = 0;
                }
            }
        }
    }
}
