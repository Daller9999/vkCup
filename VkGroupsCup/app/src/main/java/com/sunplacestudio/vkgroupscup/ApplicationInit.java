package com.sunplacestudio.vkgroupscup;

import android.app.Application;

import com.vk.sdk.VKSdk;

public class ApplicationInit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
