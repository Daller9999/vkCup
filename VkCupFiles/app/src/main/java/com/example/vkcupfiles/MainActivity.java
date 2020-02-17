
package com.example.vkcupfiles;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.vkcupfiles.FileHelper.FileFinder;
import com.example.vkcupfiles.FileHelper.OnFileFound;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnFileFound {

    private RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
            for (String permissoin : permissions)
                if (ContextCompat.checkSelfPermission(this, permissoin) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(new String[]{permissoin}, 1);
        }

        FileFinder fileFinder = new FileFinder(this);
        fileFinder.start();

        recyclerAdapter = new RecyclerAdapter(getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(recyclerAdapter);

    }

    private int fileCount = 0;

    @Override public void onFileFounded(FileData fileData) {
        runOnUiThread(() -> {
            String text = fileData.getName() + " ; " + fileData.getSize();
            recyclerAdapter.addRow(text);
        });

        Log.i("mesUri", "file founded : " + fileData.getName() + " ; type is : " + fileData.getType() + " ; path is : " + fileData.getPath());
        fileCount++;
    }

    @Override public void onOver() {
        Log.i("mesUri", "file founded = " + fileCount);
    }
}
