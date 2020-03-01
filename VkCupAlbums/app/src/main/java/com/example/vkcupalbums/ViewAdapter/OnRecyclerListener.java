package com.example.vkcupalbums.ViewAdapter;

import com.example.vkcupalbums.PhotoInfo;

public interface OnRecyclerListener {
    void onItemClick(int id);
    void onPhotoClick(PhotoInfo photoInfo);
    void onLongClick();
    void onRemove(int[] ids);
}
