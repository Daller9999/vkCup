package com.example.vkcupalbums.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vkcupalbums.AlbumInfo;
import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.R;
import com.example.vkcupalbums.ViewAdapter.OnRecyclerListener;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPhotoAlbum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FragmentAlbums extends Fragment {

    private int userId;

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private boolean edit = false;

    private Button buttonEdit;
    private Button buttonAddAlbum;
    private Button buttonStopEdit;

    private TextView textViewDocs;
    private TextView textViewEdit;

    private List<AlbumInfo> albumInfoList = new ArrayList<>();

    private final Handler handler = new Handler();

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapter = new RecyclerAdapter(getContext());
        recyclerView.setAdapter(recyclerAdapter);

        userId = Integer.valueOf(VKAccessToken.currentToken().userId);

        if (albumInfoList.isEmpty())
            loadUserAlbums();
        else
            recyclerAdapter.setList(albumInfoList);

        buttonEdit = view.findViewById(R.id.buttonEdit);
        buttonAddAlbum = view.findViewById(R.id.buttonAddAlbum);
        buttonStopEdit = view.findViewById(R.id.buttonStopEdit);
        textViewDocs = view.findViewById(R.id.mainTextDocs);
        textViewEdit = view.findViewById(R.id.secondTextDocs);

        buttonEdit.setOnClickListener((v) -> setEdit());
        buttonStopEdit.setOnClickListener((v) -> setEdit());

        recyclerAdapter.setOnRecyclerListener(new OnRecyclerListener() {
            @Override public void onRemove(int[] ids) {
                new ThreadRemove(ids).start();
            }

            @Override public void onLongClick() {
                if (!edit) setEdit();
            }

            @Override public void onItemClick(int id) {
                if (getActivity() != null)
                    ((MainActivity) getActivity()).loadFragmentPhoto(id);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        final EditText editTextName = new EditText(view.getContext());
        builder.setTitle("Введите имя нового альбома");
        builder.setPositiveButton("Создать группу", (dialogInterface, i) -> {
            String name = editTextName.getText().toString();
            if (name.isEmpty()) name = "Новый альбом";
            createAlbumApi(name);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Отмена", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setView(editTextName);
        AlertDialog alertDialog = builder.create();

        buttonAddAlbum.setOnClickListener((v) -> {
            alertDialog.show();
        });

        return view;
    }

    private class ThreadRemove extends Thread {

        private int[] ids;
        private boolean wait = true;
        private boolean error = false;

        ThreadRemove(int[] ids) {
            Vector<Integer> v = new Vector<>();
            for (int i : ids)
                v.addElement(i);
            this.ids = ids;
            List<AlbumInfo> albumInfos = new ArrayList<>();
            for (AlbumInfo albumInfo : albumInfoList)
                if (!v.contains(albumInfo.getId()))
                    albumInfos.add(albumInfo);
            albumInfoList = new ArrayList<>(albumInfos);
        }

        @Override public void run() {
            try {
                for (int id : ids) {
                    VKRequest vkRequest = new VKRequest("photos.deleteAlbum", VKParameters.from(VKApiConst.ALBUM_ID, id));
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
                Toast.makeText(getContext(), "Не удалось удалить сообщества", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void createAlbumApi(String name) {
        VKRequest vkRequest = new VKRequest("photos.createAlbum",
                VKParameters.from(
                        VKApiConst.OWNER_ID, userId,
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
                    albumInfoList.add(albumInfo);
                    handler.post(() -> recyclerAdapter.addAlbumInfo(albumInfo));
                } catch (JSONException ex) {
                    Log.e("mesUri", "json error : " + ex.getMessage());
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getContext(),"Произошла ошибка, не удалось создать группу, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEdit() {
        edit = !edit;
        int visibility1 = edit ? View.GONE : View.VISIBLE;
        buttonEdit.setVisibility(visibility1);
        buttonAddAlbum.setVisibility(visibility1);
        textViewDocs.setVisibility(visibility1);

        int visibility2 = edit ? View.VISIBLE : View.GONE;
        buttonStopEdit.setVisibility(visibility2);
        textViewEdit.setVisibility(visibility2);

        recyclerAdapter.setEdit(edit);
    }

    private void loadUserAlbums() {
        new ThreadLoadData().start();
    }

    private class ThreadLoadData extends Thread {
        private int offset = 0;
        private int count = 50;
        private boolean wait = true;

        ThreadLoadData() { }

        @Override public void run() {

            while (count > 0) {
                VKRequest vkRequest = new VKRequest("photos.getAlbums",
                        VKParameters.from(VKApiConst.OWNER_ID, userId, "need_system", 1, "need_covers", 1, "offset", offset, "count", count));
                vkRequest.executeWithListener(vkRequestListener);
                try {
                    while (wait)
                        sleep(50);
                } catch (InterruptedException ex) {
                    //
                }
            }
            handler.post(() -> recyclerAdapter.setList(albumInfoList));

            int pos = -1;
            int rowNow = 0;
            for (int i = 0; i < albumInfoList.size(); i++) {
                String http = albumInfoList.get(i).getHttp();

                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(http).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("mesUri", "error to load image : " + e.getMessage());
                }
                pos++;

                final int row = rowNow;
                final int column = pos;
                final Bitmap bitmap = mIcon11;
                handler.post(() -> recyclerAdapter.setImageBitmap(row, column, bitmap));

                if (pos == 1) {
                    rowNow++;
                    pos = -1;
                }
            }
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
    }
}
