package com.sunplacestudio.vkcupvideoqrcode.Fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sunplacestudio.vkcupvideoqrcode.MainActivity;
import com.sunplacestudio.vkcupvideoqrcode.R;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.LOG_TAG;
import static com.sunplacestudio.vkcupvideoqrcode.MainActivity.getDp;


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

    private boolean isSound = true;

    public static String formatTime(int timeMs) {
        int sec = timeMs / 1000;
        int min = sec / 60;

        int fsec = sec % 60;
        int fmin = min % 60;
        int fhour = min / 60;

        return String.format("%02d:%02d:%02d", fhour, fmin, fsec);
    }

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_video, container, false);
        videoView = view.findViewById(R.id.videoView);
        layout = view.findViewById(R.id.linearLayoutBitmaps);

        TextView textViewCurrentTime = view.findViewById(R.id.textViewCurrentTime);
        textViewCurrentTime.setText("00:00:01");
        TextView textViewLastTime = view.findViewById(R.id.textViewLastTime);

        EditVideoView editVideoView = view.findViewById(R.id.frameView);
        editVideoView.setOnPercentSwipeListener(new EditVideoView.OnPercentSwipeListener() {
            @Override public void onMiddle(int percent) {
                if (isSeen) return;

                int mills = normalDuration * percent / 100;
                videoView.seekTo(mills);

                String text1 = formatTime(mills);
                String text2 = "-" + formatTime(normalDuration - mills);
                textViewCurrentTime.setText(text1);
                textViewLastTime.setText(text2);
            }

            @Override public void onLeft(int percent) {
                videoStart = duration * (long) percent / 100;
            }

            @Override public void onRight(int percent) {
                videoEnd = duration * (long) percent / 100;
            }
        });
        CustomImageView customImageView = view.findViewById(R.id.imageButtonArrow);
        editVideoView.setCustomImageView(customImageView);

        mediaMetadataRetriever.setDataSource(fileEdit.getPath());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        normalDuration = Integer.valueOf(time);
        duration = Long.valueOf(time) * 1000L;
        textViewLastTime.setText(formatTime(normalDuration));

        layout.setOnSizeListener((w, h) -> {
            width = w;
            loadImages();
            layout.setOnSizeListener(null);
        });

        Button buttonSound = view.findViewById(R.id.buttonSoundVideo);
        buttonSound.setOnClickListener((v) -> {
            isSound = !isSound;
            buttonSound.setBackgroundResource(isSound ? R.mipmap.ic_sound_on_shadow_48 : R.mipmap.ic_sound_off_shadow_48);
        });

        Button buttonClose = view.findViewById(R.id.buttonCancelVideo);
        buttonClose.setOnClickListener((v) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).loadCameraFragment();
        });

        Button buttonSave = view.findViewById(R.id.buttonSaveVideo);
        buttonSave.setOnClickListener((v) -> {
            try {
                int start = (int) (videoStart / 1000L);
                int end = (int) (videoEnd / 1000L);
                String pathTo = getVideoFilePath(getContext());
                String pathFrom = fileEdit.getPath();
                genVideoUsingMuxer(pathFrom, pathTo, start, end, isSound);
            } catch (IOException ex) {
                //
            }
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
        videoView.seekTo(1000);
    }

    private Bitmap getBitmapAt(long mills) {
        return mediaMetadataRetriever.getFrameAtTime(mills, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    }

    public void setFileEdit(File fileEdit) {
        this.fileEdit = fileEdit;
    }


    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/")) + "videoCropAt" + getTime() + ".mp4";
    }

    public static String getTime() {
        return new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     *                 negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     *                 no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void genVideoUsingMuxer(String srcPath, String dstPath, int startMs, int endMs, boolean useAudio) throws IOException {
        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<>(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            boolean selectCurrentTrack = false;
            if (mime.startsWith("audio/") && useAudio)
                selectCurrentTrack = true;
            else if (mime.startsWith("video/"))
                selectCurrentTrack = true;

            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferSize = newSize > bufferSize ? newSize : bufferSize;
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        // Set up the orientation and starting time for extractor.
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcPath);
        String degreesString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        int offset = 0;
        int trackIndex = -1;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        try {
            muxer.start();
            while (true) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {
                    Log.i(LOG_TAG, "Saw input EOS.");
                    bufferInfo.size = 0;
                    break;
                } else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                        Log.i(LOG_TAG, "The current sample is over the trim end time.");
                        break;
                    } else {
                        bufferInfo.flags = extractor.getSampleFlags();
                        trackIndex = extractor.getSampleTrackIndex();
                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                        extractor.advance();
                    }
                }
            }
            muxer.stop();
        } catch (IllegalStateException e) {
            Log.i(LOG_TAG, "The source video file is malformed");
        } finally {
            muxer.release();
        }
        Toast.makeText(getContext(), "Видео сохранено по пути : " + dstPath, Toast.LENGTH_SHORT).show();
    }
}
