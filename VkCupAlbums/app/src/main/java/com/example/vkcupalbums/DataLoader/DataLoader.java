package com.example.vkcupalbums.DataLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.vkcupalbums.Objects.AlbumInfo;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhotoAlbum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

import static java.lang.Thread.sleep;

public final class DataLoader {

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ArrayDeque<ThreadRunners> threadRunners = new ArrayDeque<>();
    private boolean isOver = true;

    private static final DataLoader dataLoader = new DataLoader();

    public static DataLoader getInstance() { return dataLoader; }

    public synchronized void loadPhotoData(ThreadPhotoLoader.OnPhotoLoad onPhotoLoad) {
        addLoad(new ThreadPhotoLoader(onPhotoLoad, handler, onOverLoad));
    }

    public synchronized void loadDocuments(ThreadFilesLoad.OnDocsLoad onDocsLoad) {
        addLoad(new ThreadFilesLoad(onDocsLoad, handler, onOverLoad));
    }

    public synchronized void removeAlbums(int ids[]) {
        addLoad(new ThreadRemoveAlbums(ids, handler, onOverLoad));
    }

    public synchronized void createAlbum(String name, ThreadCreateAlbum.OnAlbumCreated onAlbumCreated) {
        addLoad(new ThreadCreateAlbum(name, onAlbumCreated, handler, onOverLoad));
    }

    private OnOverLoad onOverLoad = new OnOverLoad() {
        @Override
        public synchronized void onOverLoad() {
            if (!threadRunners.isEmpty()) {
                executorService.execute(threadRunners.getFirst());
                threadRunners.removeFirst();
            }
            isOver = true;
        }

        @Override
        public synchronized void onMessage(String text) {

        }
    };

    private synchronized void addLoad(ThreadRunners threadRunner) {
        int size = threadRunners.size();
        threadRunners.addLast(threadRunner);
        if (size == 0 && isOver) {
            isOver = false;
            executorService.execute(threadRunners.getFirst());
            threadRunners.removeFirst();
        }
    }

    private Handler handler = new Handler();

}
