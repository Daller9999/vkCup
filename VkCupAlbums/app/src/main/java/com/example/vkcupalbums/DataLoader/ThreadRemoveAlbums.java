package com.example.vkcupalbums.DataLoader;

import android.widget.Toast;

import com.example.vkcupalbums.Fragments.Photo.FragmentAlbums;
import com.example.vkcupalbums.Objects.AlbumInfo;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import android.os.Handler;

import static java.lang.Thread.sleep;

public class ThreadRemoveAlbums extends ThreadRunners {

    private int[] ids;
    private boolean wait = true;
    private boolean error = false;

    ThreadRemoveAlbums(int[] ids, Handler handler, OnOverLoad onOverLoad) {
        super(onOverLoad, handler);
        this.ids = ids;
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
            ThreadRemoveAlbums.this.error = true;
            // Toast.makeText(getContext(), "Не удалось удалить сообщества", Toast.LENGTH_SHORT).show();
        }
    };

    public interface OnRemovedAlbums {
        void onRemove(String message);
    }
}
