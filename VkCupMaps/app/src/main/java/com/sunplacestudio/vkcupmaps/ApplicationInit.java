package com.sunplacestudio.vkcupmaps;

import android.app.Application;

import com.vk.sdk.VKSdk;

public class ApplicationInit extends Application {

    @Override public void onCreate() {
        super.onCreate();
        VKSdk.customInitialize(this, R.integer.com_vk_sdk_AppId, "5.103");
    }
}
