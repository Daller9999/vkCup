package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.sunplacestudio.vkcupvideoqr.R;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sunplacestudio.vkcupvideoqr.MainActivity.getDp;

public class FragmentEdit extends Fragment {

    private File fileEdit;
    private int normalDuration;
    private long duration;
    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CustomLinearLayout layout;
    private VideoView videoView;
    private int widthConst = 30;
    private int width;
    private volatile boolean isSeen = false;

    private long videoStart;
    private long videoEnd;
    private long currentFrame;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_video, container, false);
        videoView = view.findViewById(R.id.videoView);
        layout = view.findViewById(R.id.linearLayoutBitmaps);
        EditViewVideo editViewVideo = view.findViewById(R.id.frameView);
        editViewVideo.setOnPercentSwipeListener(new EditViewVideo.OnPercentSwipeListener() {
            @Override public void onMiddle(int percent) {
                if (isSeen) return;

                int mills = normalDuration * percent / 100;
                videoView.seekTo(mills);
            }

            @Override public void onLeft(int percent) {
                videoStart = duration * (long) percent / 100;
            }

            @Override public void onRight(int percent) {
                videoStart = duration * (long) percent / 100;
            }
        });
        CustomImageView customImageView = view.findViewById(R.id.imageButtonArrow);
        editViewVideo.setCustomImageView(customImageView);

        if (fileEdit == null) return view;

        mediaMetadataRetriever.setDataSource(fileEdit.getPath());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        normalDuration = Integer.valueOf(time);
        duration = Long.valueOf(time) * 1000L;

        layout.setOnSizeListener((w, h) -> {
            width = w;
            loadImages();
            layout.setOnSizeListener(null);
        });

        return view;
    }

    private Handler handler = new Handler();
    private void loadImages() {
        widthConst = getDp(getContext(),30);
        long count = width / widthConst;
        final long step = duration / count;

        executorService.execute(() -> {
            for (long i = 0; i < duration; i += step) {
                final ImageView imageViewPreview = new ImageView(getContext());
                Bitmap bitmap = getBitmapAt(i);
                imageViewPreview.setLayoutParams(new LinearLayout.LayoutParams(widthConst, ViewGroup.LayoutParams.MATCH_PARENT));
                imageViewPreview.setImageBitmap(bitmap);
                handler.post(() -> layout.addView(imageViewPreview));
            }
            handler.post(() -> setupVideo());
        });
    }

    private void setupVideo() {
        videoView.requestFocus();
        Uri uri = Uri.parse(fileEdit.getPath());
        videoView.setVideoURI(uri);
    }

    private Bitmap getBitmapAt(long mills) {
        return mediaMetadataRetriever.getFrameAtTime(mills, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    }

    public void setFileEdit(File fileEdit) {
        this.fileEdit = fileEdit;
    }

    @Override public void onResume() {
        super.onResume();

        // loadImages();
    }
}
