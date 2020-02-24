package com.example.vkcupalbums.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.PhotoInfo;
import com.example.vkcupalbums.R;
import com.example.vkcupalbums.ViewAdapter.OnRecyclerListener;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapterPhotos;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// https://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk

import static android.app.Activity.RESULT_OK;
import static java.lang.Thread.sleep;

public class FragmentPhotoAlbum extends Fragment {

    private int albumId;
    private int userId;
    private RecyclerAdapterPhotos recyclerAdapterPhotos;
    private boolean edit = false;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Button buttonBack;
    private Button buttonAddPhoto;
    private Button buttonStopEdit;
    private TextView textViewEdit;

    private final Handler handler = new Handler();

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
        recyclerAdapterPhotos.setOnRecyclerListener(new OnRecyclerListener() {
            @Override public void onItemClick(int id) {}

            @Override public void onRemove(int[] ids) {
                executorService.execute(new ThreadRemove(ids));
            }

            @Override public void onLongClick() {
                if (!edit) setEdit();
            }
        });
        recyclerView.setAdapter(recyclerAdapterPhotos);

        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto);
        buttonAddPhoto.setOnClickListener((v) -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 111);
        });

        buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).popBackStack();
        });

        textViewEdit = view.findViewById(R.id.secondTextDocs);
        buttonStopEdit = view.findViewById(R.id.buttonStopEdit);
        buttonStopEdit.setOnClickListener((v) -> setEdit());

        return view;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 111:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    sendToServer(selectedImage);
                }
        }
    }

    private void sendToServer(Uri uri) {
        String path = getPath(uri);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        VKUploadImage vkUploadImage = new VKUploadImage(bitmap, VKImageParameters.pngImage());
        VKApi.uploadAlbumPhotoRequest(vkUploadImage, albumId, 0).executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response); // response => VkApiPhoto
                try {
                    JSONArray jsonArray = ((JSONArray) response.json.get("response"));
                    VKApiPhoto vkApiPhoto = new VKApiPhoto();
                    vkApiPhoto = vkApiPhoto.parse((JSONObject) jsonArray.get(0));
                    executorService.execute(new ThreadLoadPhoto(vkApiPhoto));
                } catch (JSONException ex) {
                    //
                }
                // recyclerAdapterPhotos.addPhotoInfo();
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getContext(), "Не удалось загрузить фото", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void setEdit() {
        edit = !edit;
        recyclerAdapterPhotos.setEdit(edit);
        int visibility1 = edit ? View.VISIBLE : View.GONE;
        int visibility2 = edit ? View.GONE : View.VISIBLE;
        buttonStopEdit.setVisibility(visibility1);
        textViewEdit.setVisibility(visibility1);
        buttonAddPhoto.setVisibility(visibility2);
        buttonBack.setVisibility(visibility2);
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    private void loadPhotoAlbum() {
        executorService.execute(new ThreadLoadPhotoData());
    }

    private class ThreadLoadPhotoData implements Runnable {

        private int offset = 0;
        private int count = 1000;
        private boolean wait = false;
        private List<VKApiPhoto> vkApiPhotos = new ArrayList<>();

        @Override public void run() {
            while (count > 0) {
                VKRequest vkRequest = new VKRequest("photos.get",
                        VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.ALBUM_ID, albumId, VKApiConst.COUNT, 1000, "offset", offset));
                vkRequest.executeWithListener(vkRequestListener);
                wait = true;
                try {
                    while (wait)
                        sleep(50);
                } catch (InterruptedException ex) {
                    //
                }
            }
            executorService.execute(new ThreadLoadData(vkApiPhotos));
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
    }

    private class ThreadRemove implements Runnable {

        private int[] ids;
        private boolean wait = true;
        private boolean error = false;

        ThreadRemove(int[] ids) {
            this.ids = ids;
        }

        @Override public void run() {
            try {
                for (int id : ids) {
                    VKRequest vkRequest = new VKRequest("photos.delete", VKParameters.from(VKApiConst.OWNER_ID, userId, "photo_id", id));
                    wait = true;
                    vkRequest.executeWithListener(vkRequestListener);
                    while (wait) {
                        sleep(50);
                        if (error)
                            return;
                    }
                    sleep(300);
                }
            } catch (InterruptedException ex) {
                //
            }
        }

        private VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                wait = false;
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                ThreadRemove.this.error = true;
                Toast.makeText(getContext(), "Не удалось удалить фото", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private class ThreadLoadData implements Runnable {
        private List<VKApiPhoto> vkApiPhotos;

        ThreadLoadData(List<VKApiPhoto> vkApiPhotos) {
            this.vkApiPhotos = vkApiPhotos;
            List<PhotoInfo> photoInfoList = new ArrayList<>();
            for (VKApiPhoto vkApiPhoto : vkApiPhotos)
                photoInfoList.add(new PhotoInfo(vkApiPhoto.id));
            handler.post(() -> recyclerAdapterPhotos.setList(photoInfoList));
        }

        @Override public void run() {
            int pos = -1;
            int rowNow = 0;
            for (int i = 0; i < vkApiPhotos.size(); i++) {
                VKApiPhoto vkApiPhoto = vkApiPhotos.get(i);

                Bitmap bitmap = loadPhoto(vkApiPhoto);
                pos++;

                final int row = rowNow;
                final int column = pos;

                handler.post(() -> recyclerAdapterPhotos.setImageBitmap(row, column, bitmap));

                if (pos == 2) {
                    rowNow++;
                    pos = -1;
                }
            }
        }
    }

    private class ThreadLoadPhoto implements Runnable {

        private VKApiPhoto vkApiPhoto;

        private ThreadLoadPhoto(VKApiPhoto vkApiPhoto) {
            this.vkApiPhoto = vkApiPhoto;
        }

        @Override public void run() {
            Bitmap bitmap = loadPhoto(vkApiPhoto);
            PhotoInfo photoInfo = new PhotoInfo(vkApiPhoto.id);
            photoInfo.setBitmap(bitmap);
            handler.post(() -> recyclerAdapterPhotos.addPhotoInfo(photoInfo));
        }
    }

    private Bitmap loadPhoto(VKApiPhoto vkApiPhoto) {
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
        return mIcon11;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

}
