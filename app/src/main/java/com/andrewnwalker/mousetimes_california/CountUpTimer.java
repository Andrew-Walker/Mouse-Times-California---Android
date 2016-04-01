package com.andrewnwalker.mousetimes_california;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by Andrew Walker on 13/02/2016.
 */

public abstract class CountUpTimer {
    abstract public void onTick(long elapsedTime);

    private static final int MSG = 1;
    private final long interval;
    private long base;

    public CountUpTimer(long interval) {
        this.interval = interval;
    }

    public void start() {
        base = SystemClock.elapsedRealtime();
        handler.sendMessage(handler.obtainMessage(MSG));
    }

    public void stop() {
        handler.removeMessages(MSG);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CountUpTimer.this) {
                long elapsedTime = SystemClock.elapsedRealtime() - base;
                onTick(elapsedTime);
                sendMessageDelayed(obtainMessage(MSG), interval);
            }
        }
    };
}