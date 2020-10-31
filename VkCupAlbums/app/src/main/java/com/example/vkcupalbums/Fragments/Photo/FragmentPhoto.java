package com.example.vkcupalbums.Fragments.Photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.vkcupalbums.MainActivity;
import com.example.vkcupalbums.Objects.PhotoInfo;
import com.example.vkcupalbums.R;

public class FragmentPhoto extends Fragment {

    private PhotoInfo photoInfo;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageBitmap(photoInfo.getBitmap());

        Button buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> {
            getFragmentManager().popBackStack();
        });
        return view;
    }

    public void setPhoto(PhotoInfo photoInfo) {
        this.photoInfo = photoInfo;
    }
}
