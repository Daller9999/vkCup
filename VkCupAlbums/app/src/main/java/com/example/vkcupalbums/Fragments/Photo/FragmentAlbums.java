package com.example.vkcupalbums.Fragments.Photo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vkcupalbums.DataLoader.DataLoader;
import com.example.vkcupalbums.Objects.AlbumInfo;
import com.example.vkcupalbums.Objects.PhotoInfo;
import com.example.vkcupalbums.R;
import com.example.vkcupalbums.ViewAdapter.OnRecyclerListener;
import com.example.vkcupalbums.ViewAdapter.RecyclerAdapter;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Thread.sleep;

public class FragmentAlbums extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private boolean edit = false;

    private Button buttonEdit;
    private Button buttonAddAlbum;
    private Button buttonStopEdit;

    private TextView textViewDocs;
    private TextView textViewEdit;

    private List<AlbumInfo> albumInfoList = new ArrayList<>();

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapter = new RecyclerAdapter(getContext());
        recyclerView.setAdapter(recyclerAdapter);

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
                Vector<Integer> v = new Vector<>();
                for (int i : ids)
                    v.addElement(i);

                List<AlbumInfo> albumInfos = new ArrayList<>();
                for (AlbumInfo albumInfo : albumInfoList)
                    if (!v.contains(albumInfo.getId()))
                        albumInfos.add(albumInfo);
                albumInfoList = new ArrayList<>(albumInfos);

                DataLoader.getInstance().removeAlbums(ids);
            }

            @Override public void onLongClick() {
                if (!edit) setEdit();
            }

            @Override public void onItemClick(int id) {
                FragmentPhotoAlbum fragmentPhotoAlbum = new FragmentPhotoAlbum();
                fragmentPhotoAlbum.setAlbumId(id);
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.to_left_in, R.anim.to_left_out, R.anim.to_right_in, R.anim.to_right_out)
                        .replace(R.id.container_photo, fragmentPhotoAlbum)
                        .addToBackStack(FragmentPhotoAlbum.class.getName())
                        .commit();
            }

            @Override public void onPhotoClick(PhotoInfo photoInfo) { }
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

    private void createAlbumApi(String name) {
        DataLoader.getInstance().createAlbum(name, albumInfo -> {
            albumInfoList.add(albumInfo);
            recyclerAdapter.addAlbumInfo(albumInfo);
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
        DataLoader.getInstance().loadAlbums(albumInfoList -> {
            this.albumInfoList = albumInfoList;
            recyclerAdapter.setList(albumInfoList);
        });
    }
}
