package com.example.vkcupalbums.Fragments.Photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vkcupalbums.R;

public class FragmentMainPhoto extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_photo, container, false);
        getFragmentManager().beginTransaction().replace(R.id.container_photo, new FragmentAlbums()).commit();
        return view;
    }
}
