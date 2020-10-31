package com.example.vkcupalbums.DataLoader;

import java.util.ArrayDeque;
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

    public synchronized void loadAlbums(ThreadAlbumLoader.OnAlbumsLoad onAlbumsLoad) {
        addLoad(new ThreadAlbumLoader(onAlbumsLoad, handler, onOverLoad));
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

    public synchronized void loadPhotoAlbums(int albumId, ThreadLoadAlbumPhotos.OnPhotoLoaded onPhotoLoaded) {
        addLoad(new ThreadLoadAlbumPhotos(handler, onOverLoad, albumId, onPhotoLoaded));
    }

    public synchronized void removePhotos(int[] ids) {
        addLoad(new ThreadRemovePhotos(ids, handler, onOverLoad));
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
