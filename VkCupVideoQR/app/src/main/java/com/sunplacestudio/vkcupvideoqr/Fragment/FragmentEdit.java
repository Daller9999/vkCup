package com.sunplacestudio.vkcupvideoqr.Fragment;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sunplacestudio.vkcupvideoqr.R;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentEdit extends Fragment {

    private File fileEdit;
    private long duration;
    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CustomLinearLayout layout;
    private ImageView imageView;
    private int widthConst = 30;
    private int width;
    private int heigthView;

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_video, container, false);
        imageView = view.findViewById(R.id.imageView);
        layout = view.findViewById(R.id.linearLayoutBitmaps);
        EditViewVideo viewLeft = view.findViewById(R.id.frameView);
        CustomImageView buttonImageLeft = view.findViewById(R.id.imageButtonLeft);
        buttonImageLeft.setOnMoveLisener((x) -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) buttonImageLeft.getLayoutParams();
            viewLeft.setLeftDraw(x - layoutParams.leftMargin);
        });
        heigthView = getDp(56);

        if (fileEdit == null) return view;

        mediaMetadataRetriever.setDataSource(fileEdit.getPath());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.valueOf(time) * 1000L;
        imageView.setImageBitmap(getBitmapAt(0));

        layout.setOnSizeListener((w, h) -> {
            width = w;
            loadImages();
            layout.setOnSizeListener(null);
        });

        return view;
    }

    public int getDp(int len) { return (int) (len * getContext().getResources().getDisplayMetrics().density + 0.5f); }

    private Handler handler = new Handler();
    private void loadImages() {
        widthConst = getDp(30);
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
        });
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
