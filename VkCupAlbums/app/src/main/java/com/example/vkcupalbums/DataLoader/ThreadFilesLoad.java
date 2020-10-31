package com.example.vkcupalbums.DataLoader;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupalbums.Objects.VkDocsData;
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

import static java.lang.Thread.sleep;

public class ThreadFilesLoad extends ThreadRunners {
    private int offest = 0;
    private int count = 2000;
    private boolean wait;
    private List<VkDocsData> vkDocsDataList = new ArrayList<>();
    private OnDocsLoad onDocsLoad;

    ThreadFilesLoad(OnDocsLoad onDocsLoad, Handler handler, OnOverLoad onOverLoad) {
        super(onOverLoad, handler);
        this.onDocsLoad = onDocsLoad;
    }

    @Override public void run() {
        while (count > 0) {
            VKRequest vkRequest = new VKRequest("docs.get", VKParameters.from(VKApiConst.COUNT, 2000, VKApiConst.OWNER_ID, Integer.valueOf(VKAccessToken.currentToken().userId), "offset", offest));
            vkRequest.executeWithListener(vkRequestListener);
            wait = true;
            try {
                while (wait)
                    sleep(50);
            } catch (InterruptedException ex) {
                //
            }
        }
        onOverLoad.onOverLoad();
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
                handler.post(() -> onDocsLoad.onLoad(vkDocsDataList));
                offest += 2000;
                wait = false;
            } catch (JSONException ex) {
                // Toast.makeText(getContext(), "Не удалось загрузить файлы, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
            }
        }

        @Override public void onError(VKError error) {
            super.onError(error);
            Log.e("mesUri", error.toString());
            wait = false;
            // Toast.makeText(getContext(), "Не удалось загрузить файлы, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
        }
    };

    public interface OnDocsLoad {
        void onLoad(List<VkDocsData> vkDocsDataList);
    }
}
