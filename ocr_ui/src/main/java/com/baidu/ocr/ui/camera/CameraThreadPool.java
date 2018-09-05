package com.baidu.ocr.ui.camera;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraThreadPool {
    private static Timer timerFocus = null;
    private static final long cameraScanInterval = 2000;
    private static int poolCount = Runtime.getRuntime().availableProcessors();
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolCount);

    public static void execute(Runnable runnable) {
        fixedThreadPool.execute(runnable);
    }

    public static void createAutoFocusTimerTask(final Runnable runnable) {
        if (timerFocus != null) {
            return;
        }
        timerFocus = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timerFocus.scheduleAtFixedRate(task, 0, cameraScanInterval);
    }

    public static void cancelAutoFocusTimer() {
        if (timerFocus != null) {
            timerFocus.cancel();
            timerFocus = null;
        }
    }
}