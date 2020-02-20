package com.example.vkcupalbums.ViewAdapter;

public interface OnRecyclerListener {
    void onItemClick(int id);
    void onLongClick();
    void onRemove(int[] ids);
}
