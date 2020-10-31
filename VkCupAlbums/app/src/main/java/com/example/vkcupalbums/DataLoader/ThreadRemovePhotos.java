package com.example.vkcupalbums.DataLoader;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupalbums.Fragments.Photo.FragmentPhotoAlbum;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import static java.lang.Thread.sleep;

public class ThreadRemovePhotos extends ThreadRunners {

    private int[] ids;
    private boolean wait = true;
    private boolean error = false;

    ThreadRemovePhotos(int[] ids, Handler handler, OnOverLoad onOverLoad) {
        super(onOverLoad, handler);
        this.ids = ids;
    }

    @Override public void run() {
        try {
            for (int id : ids) {
                VKRequest vkRequest = new VKRequest("photos.delete", VKParameters.from(VKApiConst.OWNER_ID, Integer.valueOf(VKAccessToken.currentToken().userId), "photo_id", id));
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
            ex.printStackTrace();
        }
        onOverLoad.onOverLoad();
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
            Log.e("mesUri", error.toString());
            ThreadRemovePhotos.this.error = true;
            // Toast.makeText(getContext(), "Не удалось удалить фото", Toast.LENGTH_SHORT).show();
        }
    };

}
