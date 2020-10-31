package com.example.vkcupalbums;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.vkcupalbums.Fragments.Documents.FragmentListFiles;
import com.example.vkcupalbums.Fragments.Documents.FragmentMainDocuments;
import com.example.vkcupalbums.Fragments.Photo.FragmentAlbums;
import com.example.vkcupalbums.Fragments.Photo.FragmentMainPhoto;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    // private SparseArray<Fragment> fragmentSparseArray = new SparseArray<>();
    private List<Fragment> mFragmentList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentList.add(new FragmentMainPhoto());
        mFragmentList.add(new FragmentMainDocuments());
    }

    @Override public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }


    @Override public int getCount() { return mFragmentList.size(); }
}

