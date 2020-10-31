package com.example.vkcupalbums.ViewAdapter;

import com.example.vkcupalbums.Objects.PhotoInfo;

public interface OnRecyclerListener {
    void onItemClick(int id);
    void onPhotoClick(PhotoInfo photoInfo);
    void onLongClick();
    void onRemove(int[] ids);
}
