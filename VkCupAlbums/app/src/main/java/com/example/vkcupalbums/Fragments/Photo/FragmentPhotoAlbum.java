package com.example.vkcupalbums.Fragments.Photo;

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

import com.example.vkcupalbums.DataLoader.DataLoader;
import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.Objects.PhotoInfo;
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

import static android.app.Activity.RESULT_OK;
import static java.lang.Thread.sleep;

// https://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk

public class FragmentPhotoAlbum extends Fragment {

    private int albumId;
    private RecyclerAdapterPhotos recyclerAdapterPhotos;
    private boolean edit = false;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private List<PhotoInfo> photoInfos = new ArrayList<>();

    private Button buttonBack;
    private Button buttonAddPhoto;
    private Button buttonStopEdit;
    private TextView textViewEdit;

    private final Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_photo_albums, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapterPhotos = new RecyclerAdapterPhotos(getContext());
        recyclerAdapterPhotos.setOnRecyclerListener(new OnRecyclerListener() {
            @Override public void onItemClick(int id) {}

            @Override public void onPhotoClick(PhotoInfo photoInfo) {
                photoInfos = recyclerAdapterPhotos.getList();
                FragmentPhoto fragmentPhoto = new FragmentPhoto();
                fragmentPhoto.setPhoto(photoInfo);
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.to_left_in, R.anim.to_left_out, R.anim.to_right_in, R.anim.to_right_out)
                        .replace(R.id.container_photo, fragmentPhoto)
                        .addToBackStack(FragmentPhoto.class.getName())
                        .commit();
            }

            @Override public void onRemove(int[] ids) {
                DataLoader.getInstance().removePhotos(ids);
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
            getFragmentManager().popBackStack();
        });

        textViewEdit = view.findViewById(R.id.secondTextDocs);
        buttonStopEdit = view.findViewById(R.id.buttonStopEdit);
        buttonStopEdit.setOnClickListener((v) -> setEdit());

        if (photoInfos.isEmpty())
            loadPhotoAlbum();
        else
            recyclerAdapterPhotos.setList(photoInfos);

        return view;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 111:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    executorService.execute(() -> sendToServer(selectedImage));
                }
        }
    }

    private void sendToServer(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), uri);
            VKUploadImage vkUploadImage = new VKUploadImage(bitmap, VKImageParameters.pngImage());
            VKApi.uploadAlbumPhotoRequest(vkUploadImage, albumId, 0).executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONArray jsonArray = ((JSONArray) response.json.get("response"));
                        VKApiPhoto vkApiPhoto = new VKApiPhoto();
                        vkApiPhoto = vkApiPhoto.parse((JSONObject) jsonArray.get(0));
                        if (executorService != null)
                            executorService.execute(new ThreadLoadPhoto(vkApiPhoto));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Toast.makeText(getContext(), "Не удалось загрузить фото", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        DataLoader.getInstance().loadPhotoAlbums(albumId, vkApiPhotos -> {
            List<PhotoInfo> photoInfoList = new ArrayList<>();
            for (VKApiPhoto vkApiPhoto : vkApiPhotos)
                photoInfoList.add(new PhotoInfo(vkApiPhoto.id, vkApiPhoto));
            recyclerAdapterPhotos.setList(photoInfoList);
        });
    }

    private class ThreadLoadPhoto implements Runnable {

        private VKApiPhoto vkApiPhoto;

        private ThreadLoadPhoto(VKApiPhoto vkApiPhoto) {
            this.vkApiPhoto = vkApiPhoto;
        }

        @Override public void run() {
            Bitmap bitmap = loadPhoto(vkApiPhoto);
            PhotoInfo photoInfo = new PhotoInfo(vkApiPhoto.id, vkApiPhoto);
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
