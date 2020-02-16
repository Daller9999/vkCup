package com.sunplacestudio.vksharephotoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            hideSelectedPhoto();
            // дальше вызываем код на отправку фото на сервер
        });

        Button buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener((v) -> hideSelectedPhoto());

        setStatusBar(Color.WHITE);
    }

    @Override // утсанавливаем тут фото в imageView
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        setStatusBar(getResources().getColor(R.color.colorGray));

        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    layout.setVisibility(View.VISIBLE);
                    imageView.setImageURI(selectedImage);
                }
        }
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
        editText.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
