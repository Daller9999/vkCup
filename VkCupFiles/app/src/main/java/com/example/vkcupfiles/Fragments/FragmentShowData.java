package com.example.vkcupfiles.Fragments;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.vkcupfiles.MainActivity;
import com.example.vkcupfiles.R;
import com.example.vkcupfiles.VkDocsData;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

public class FragmentShowData extends Fragment {

    private VkDocsData vkDocsData;

    private ImageView imageView;
    private TextView textView;
    private VideoView videoView;

    private MediaController mediaController;

    private SeekBar seekBar;
    private Button buttonStart;
    private boolean play = true;

    private ProgressDialog pDialog;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_show_data, container, false);
        imageView = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.textView);
        videoView = view.findViewById(R.id.videoView);
        seekBar = view.findViewById(R.id.seekBar);
        buttonStart = view.findViewById(R.id.buttonPlay);

        mediaController = view.findViewById(R.id.mediaController);

        Button buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener((v) -> {
            if (getActivity() != null)
                ((MainActivity) getActivity()).popBackStack();
        });

        TextView textView = view.findViewById(R.id.textViewFileName);
        textView.setText(vkDocsData.getTitle());

        if (vkDocsData.getType() == VkDocsData.GIF || vkDocsData.getType() == VkDocsData.IMAGE) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(vkDocsData.getUrl()).into(imageView);
        } else if (vkDocsData.getType() == VkDocsData.VIDEO || vkDocsData.getType() == VkDocsData.AUDIO) {
            if (vkDocsData.getType() == VkDocsData.VIDEO) {
                videoView.setVisibility(View.VISIBLE);
                MediaController mediaController = new MediaController(getContext());
                mediaController.setMediaPlayer(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoPath(vkDocsData.getUrl());
                videoView.start();
            } else {
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(vkDocsData.getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException ex) {
                    //
                }
            }
        } else {
            new DownloadFileFromURL().execute(vkDocsData.getUrl());
        }
        return view;
    }

    public void setData(VkDocsData vkDocsData) {
        this.vkDocsData = vkDocsData;
    }


    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private String path;

        @Override protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Пожалуйста подождите закачку файлов");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.show();
            File file = Environment.getExternalStorageDirectory();
            File files = new File(file.getPath() + "/" + "VkDownload");
            files.mkdir();
            path = files.getPath();
        }

        @Override protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                int length = conection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(path + "/" + vkDocsData.getTitle());

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / length));
                    output.write(data, 0, count);
                }

                output.flush();

                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("mesUri ", e.getMessage());
            }

            return null;
        }


        @Override protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            File file = new File(path + "/" + vkDocsData.getTitle());
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            if (getActivity() != null && getContext() != null) {
                Uri uri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName(), file);
                String type = getContext().getContentResolver().getType(uri);
                sharingIntent.setDataAndType(uri, type);
                startActivity(Intent.createChooser(sharingIntent, "share file with"));
                Toast.makeText(getContext(), "Файл закачан в основную директорию телефона в папку - VkDownload", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
