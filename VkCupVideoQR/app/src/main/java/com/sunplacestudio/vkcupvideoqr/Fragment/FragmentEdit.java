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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.sunplacestudio.vkcupvideoqr.MainActivity;
import com.sunplacestudio.vkcupvideoqr.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    private boolean isSound = true;

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
                videoEnd = duration * (long) percent / 100;
            }
        });
        CustomImageView customImageView = view.findViewById(R.id.imageButtonArrow);
        editViewVideo.setCustomImageView(customImageView);

        mediaMetadataRetriever.setDataSource(fileEdit.getPath());
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        normalDuration = Integer.valueOf(time);
        duration = Long.valueOf(time) * 1000L;

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
                ((MainActivity) getActivity()).popBackStack();
        });

        Button buttonSave = view.findViewById(R.id.buttonSaveVideo);
        buttonSave.setOnClickListener((v) -> {
            try {
                cropVideo();
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

    private void cropVideo() throws IOException {
        Movie movie = MovieCreator.build(fileEdit.getPath());
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());

        double startTime = videoStart;
        double endTime = videoEnd;

        /*for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                startTime = correctTimeToSyncSample(track, startTime, false);
                endTime = correctTimeToSyncSample(track, endTime, true);
            }
        }*/

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }
        Container out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(fileEdit.getPath());
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
        Toast.makeText(getContext(), "Video saved", Toast.LENGTH_SHORT).show();
        File file = new File("cropVideoNow.mp4");
        /*long start3 = System.currentTimeMillis();
        System.err.println("Building IsoFile took : " + (start2 - start1) + "ms");
        System.err.println("Writing IsoFile took  : " + (start3 - start2) + "ms");
        System.err.println("Writing IsoFile speed : " + (new File(String.format("output-%f-%f--%f-%f.mp4", startTime1, endTime1*/
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}
