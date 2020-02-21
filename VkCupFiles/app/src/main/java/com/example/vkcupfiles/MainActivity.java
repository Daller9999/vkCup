
package com.example.vkcupfiles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.vkcupfiles.FileHelper.FileFinder;
import com.example.vkcupfiles.FileHelper.OnFileFound;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiDocs;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerAdapter recyclerAdapter;
    private int userId;
    private List<VkDocsData> vkDocsDataList = new ArrayList<>();
    private List<VkDocsData> vkDocsDataListCopy = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
            for (String permissoin : permissions)
                if (ContextCompat.checkSelfPermission(this, permissoin) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(new String[]{permissoin}, 1);
        }

        String[] vkPermissions = new String[]{VKScope.DOCS};
        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(this, vkPermissions);
        } else {
            userId = Integer.valueOf(VKAccessToken.currentToken().userId);
            loadFiles();
        }

        recyclerAdapter = new RecyclerAdapter(getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(recyclerAdapter);

    }

    private int fileCount = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override public void onResult(VKAccessToken res) {
                userId = Integer.valueOf(res.userId);
                loadFiles();
                // Пользователь успешно авторизовался
            }

            @Override public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadFiles() {
        VKRequest vkRequest = new VKRequest("docs.get", VKParameters.from(VKApiConst.COUNT, 2000, VKApiConst.OWNER_ID, userId));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject jsonObject = (JSONObject) response.json.get("response");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        int id = jsonObject1.getInt("id");
                        int type = jsonObject1.getInt("type");
                        String title = jsonObject1.getString("title");
                        int size = jsonObject1.getInt("size");
                        String ext = jsonObject1.getString("ext");
                        String url = jsonObject1.getString("url");
                        int date = jsonObject1.getInt("date");

                        vkDocsDataList.add(new VkDocsData(id, type, title, size, ext, url, date, jsonObject1));
                    }
                    vkDocsDataListCopy = new ArrayList<>(vkDocsDataList);
                    recyclerAdapter.setList(vkDocsDataList);
                    loadAllOther();
                } catch (JSONException ex) {
                    //
                }
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(MainActivity.this, "Не удалось загрузить файлы, проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private Handler handler = new Handler();
    private void loadAllOther() {
        new Thread(() -> {
            int count = 0;
            for (VkDocsData vkDocsData : vkDocsDataListCopy) {
                if (vkDocsData.getType() == VkDocsData.IMAGE || vkDocsData.getType() == VkDocsData.GIF) {
                    JSONObject jsonObject = vkDocsData.getJsonObject();
                    try {
                        String http = jsonObject.getString("photo_100");
                        Bitmap bitmap = loadPhot(http);
                        final int pos = count;
                        vkDocsDataListCopy.get(pos).setBitmap(bitmap);
                        handler.post(() -> recyclerAdapter.updateImage(bitmap, pos));
                    } catch (JSONException ex) {
                        ex.getCause();
                        Log.e("mesUri", "json error : " + ex.getMessage());
                    }
                }
                count++;
            }
        }).start();
    }

    private Bitmap loadPhot(String http) {
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(http).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("mesUri", "error to load image : " + e.getMessage());
        }
        return bitmap;
    }
}
