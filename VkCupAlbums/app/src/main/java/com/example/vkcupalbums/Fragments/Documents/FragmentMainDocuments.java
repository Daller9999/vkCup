package com.example.vkcupalbums.Fragments.Documents;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vkcupalbums.R;

public class FragmentMainDocuments extends Fragment {
    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_documents, container, false);
        getFragmentManager().beginTransaction().replace(R.id.container_documents, new FragmentListFiles()).commit();
        return view;
    }
}
