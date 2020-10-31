package com.example.vkcupfiles.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.vkcupfiles.MainActivity;
import com.example.vkcupfiles.R;
import com.example.vkcupfiles.VkDocsData;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentListFiles extends Fragment {

    private RecyclerAdapter recyclerAdapter;
    private int userId;
    private List<VkDocsData> vkDocsDataList = new ArrayList<>();


    @Override public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_files_list, container, false);

        userId = Integer.valueOf(VKAccessToken.currentToken().userId);

        recyclerAdapter = new RecyclerAdapter(view.getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnRecyclerClick(new RecyclerAdapter.OnRecyclerClick() {
            @Override public void onItemClick(VkDocsData vkDocsData) {
                if (getActivity() != null)
                    ((MainActivity) getActivity()).loadDataFile(vkDocsData);
            }

            @Override public void onError(String text) {
                Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        if (vkDocsDataList.isEmpty())
            loadFiles();
        else
            recyclerAdapter.setList(vkDocsDataList);

        return view;
    }

    private class ThreadLoad extends Thread {
        private int offest = 0;
        private int count = 2000;
        private boolean wait;

        @Override public void run() {
            while (count > 0) {
                VKRequest vkRequest = new VKRequest("docs.get", VKParameters.from(VKApiConst.COUNT, 2000, VKApiConst.OWNER_ID, userId, "offset", offest));
                vkRequest.executeWithListener(vkRequestListener);
                wait = true;
                try {
                    while (wait)
                        sleep(50);
                } catch (InterruptedException ex) {
                    //
                }
            }

        }

        private VKRequest.VKRequestListener vkRequestListener = new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject jsonObject = (JSONObject) response.json.get("response");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    count = jsonArray.length();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        int id = jsonObject1.getInt("id");
                        int type = jsonObject1.getInt("type");
                        String title = jsonObject1.getString("title");
                        int size = jsonObject1.getInt("size");
                        String ext = jsonObject1.getString("ext");
                        String url = jsonObject1.getString("url");
                        int date = jsonObject1.getInt("date");
                        String https = null;
                        try {
                            if (type == VkDocsData.IMAGE || type == VkDocsData.GIF)
                                https = jsonObject1.getString("photo_100");
                        } catch (JSONException ex) {
                            //
                        }

                        vkDocsDataList.add(new VkDocsData(id, type, title, size, ext, url, https, date));
                    }
                    recyclerAdapter.addList(vkDocsDataList);
                    offest += 2000;
                    wait = false;
                } catch (JSONException ex) {
                    Toast.makeText(getContext(), "Не удалось загрузить файлы, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                wait = false;
                Toast.makeText(getContext(), "Не удалось загрузить файлы, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void loadFiles() {
        new ThreadLoad().start();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        recyclerAdapter.destroy();
    }
}
