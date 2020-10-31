package com.example.vkcupalbums.DataLoader;

import android.os.Handler;

public abstract class ThreadRunners implements Runnable {

    protected OnOverLoad onOverLoad;

    protected Handler handler;


    protected ThreadRunners(OnOverLoad onOverLoad, Handler handler) {
        this.onOverLoad = onOverLoad;
        this.handler = handler;
    }
}
