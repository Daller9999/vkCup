package com.sunplacestudio.vksharephotoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.util.VKUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    /**
     * Лайаут с выбранным изображением
     */
    private FrameLayout layout;
    /**
     * Основной лайаут, где мы жмеём на кнопку выбрать фото
     */
    private ConstraintLayout constraintLayoutMain;
    // тут всё понятно
    private ImageView imageView;
    // ввод текста
    private EditText editText;

    private Bitmap bitmap = null;
    private String text = null;

    private String key;
    private int userId;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());

        String[] permissionsPhone = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= 23)
            requestPermissions(permissionsPhone, 1);

        String[] permissions = new String[]{VKScope.PHOTOS, VKScope.WALL};
        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, permissions);
        else {
            key = VKAccessToken.currentToken().accessToken;
            userId = Integer.valueOf(VKAccessToken.currentToken().userId);
        }

        constraintLayoutMain = findViewById(R.id.main_constraint_layout);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.edit_text);
        layout = findViewById(R.id.linearLayout);

        layout.setVisibility(View.GONE);

        Button buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto);
        buttonSelectPhoto.setOnClickListener((v) -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        });

        Button buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener((v) -> {
            executorService.execute(this::loadPhotoToWall);
            hideSelectedPhoto();
        });

        Button buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener((v) -> hideSelectedPhoto());


        setStatusBar(Color.WHITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override
            public void onResult(VKAccessToken res) {
                key = res.accessToken;
                userId = Integer.valueOf(res.userId);
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Ошибка при входе в ВК, попробуйте перезайти в приложение", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
            setStatusBar(getResources().getColor(R.color.colorGray));

            switch(requestCode) {
                case 1:
                    if(resultCode == RESULT_OK){
                        Uri selectedImage = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            layout.setVisibility(View.VISIBLE);
                            imageView.setImageURI(selectedImage);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
            }
        }
    }

    private void loadPhotoToWall() {
        if (bitmap == null) return;
        text = editText.getText().toString();
        VKUploadImage vkUploadImage = new VKUploadImage(bitmap, VKImageParameters.pngImage());
        VKApi.uploadWallPhotoRequest(vkUploadImage, userId, 0).executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKPhotoArray vkApiPhotos = (VKPhotoArray) response.parsedModel;
                VKApiPhoto vkApiPhoto = vkApiPhotos.get(0);
                executorService.execute(() -> loadTextAfterLoadPhoto(vkApiPhoto.id));
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(MainActivity.this, "Не удалось загрузить фото на стену", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTextAfterLoadPhoto(int photoId) {
        VKParameters vkParameters;
        String photoData = "photo" + String.valueOf(userId) + "_" + String.valueOf(photoId);
        if (text.isEmpty())
            vkParameters = VKParameters.from(VKApiConst.OWNER_ID, userId, "attachments", photoData);
        else
            vkParameters = VKParameters.from(VKApiConst.OWNER_ID, userId, "attachments", photoData, "message", text);
        VKApi.wall().post(vkParameters).executeWithListener(new VKRequest.VKRequestListener() {
            @Override public void onComplete(VKResponse response) {
                super.onComplete(response);
                Toast.makeText(MainActivity.this, "Пост успешно опубликован", Toast.LENGTH_SHORT).show();
            }

            @Override public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(MainActivity.this, "Не удалось загрузить фото на стену", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override public void onBackPressed() {
        hideSelectedPhoto();
    }

    private void setStatusBar(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        constraintLayoutMain.setBackgroundColor(color);
    }

    private void hideSelectedPhoto() {
        layout.setVisibility(View.GONE);
        setStatusBar(Color.WHITE);
        imageView.setImageBitmap(null);
        editText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
