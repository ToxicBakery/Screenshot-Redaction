package com.ToxicBakery.app.screenshot_redaction.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.observer.ScreenshotObserver;
import com.ToxicBakery.app.screenshot_redaction.observer.ScreenshotObserver.IOnScreenShotListener;
import com.ToxicBakery.app.screenshot_redaction.observer.UriObserver;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageReader;
import com.ToxicBakery.app.screenshot_redaction.util.PermissionCheck;

public class ScreenshotService extends Service {

    private static final String TAG = "ScreenshotService";

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private ScreenshotObserver screenshotObserver;
    private boolean isObserving;

    public static void startScreenshotService(@NonNull Context context) {
        if (PermissionCheck.hasPermissions(context, REQUIRED_PERMISSIONS)) {
            Intent intent = new Intent(context, ScreenshotService.class);
            context.startService(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isObserving) {
            Log.d(TAG, "Starting observer.");
            try {
                ScreenshotContentObserver screenshotContentObserver = new ScreenshotContentObserver(getApplicationContext());
                screenshotObserver = new ScreenshotObserver(this, screenshotContentObserver);
                isObserving = true;
            } catch (UriObserver.ObserverException e) {
                Log.e(TAG, "Failed to start observer.", e);
                return START_NOT_STICKY;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping observer.");
        isObserving = false;
        screenshotObserver.stop();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Binding is not supported.");
    }

    static class ScreenshotContentObserver implements IOnScreenShotListener {

        private final Context context;

        ScreenshotContentObserver(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public void onScreenShot(Uri uri) {
            Log.d(TAG, "Screenshot added: " + uri);
            new OcrImageReader().submit(context, uri);
        }

    }

}
