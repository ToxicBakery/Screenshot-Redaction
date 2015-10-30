package com.ToxicBakery.app.screenshot_redaction.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.notification.ScreenShotNotifications;

import java.io.File;
import java.util.Date;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class ScreenshotObserver {

    private static final String[] PROJECTION = {
            Media.DATA,
            Media.DATE_ADDED
    };

    private static final String SELECTION = Media.DISPLAY_NAME + " like 'Screenshot%'";
    private static final String SORT_ORDER = Media.DATE_ADDED + " desc limit 1";

    private final UriObserver uriObserver;

    public ScreenshotObserver(@NonNull Context context,
                              @NonNull IOnScreenShotListener onScreenShotListener) throws UriObserver.ObserverException {

        ContentObserver observer = new ObserverImpl(context, onScreenShotListener);
        uriObserver = new UriObserver(context, EXTERNAL_CONTENT_URI, observer);
    }

    /**
     * Stop listening for screenshots.
     */
    public void stop() {
        uriObserver.stop();
    }

    public interface IOnScreenShotListener {

        void onScreenShot(Uri uri);

    }

    static class ObserverImpl extends ContentObserver {

        private static final String TAG = "ObserverImpl";

        private final IOnScreenShotListener onScreenShotListener;
        private final ContentResolver contentResolver;
        private final Context context;

        private long lastDate;

        public ObserverImpl(@NonNull Context context,
                            @NonNull IOnScreenShotListener onScreenShotListener) {

            super(null);

            this.context = context;
            this.onScreenShotListener = onScreenShotListener;
            contentResolver = context.getContentResolver();
            lastDate = System.currentTimeMillis();
        }

        @Override
        public void onChange(boolean selfChange) {
            Cursor cursor = contentResolver.query(EXTERNAL_CONTENT_URI, PROJECTION, SELECTION, null, SORT_ORDER);
            try {
                if (cursor != null) {
                    int idxData = cursor.getColumnIndex(Media.DATA);
                    int idxDate = cursor.getColumnIndex(Media.DATE_ADDED);

                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(idxData);
                        File file = new File(path);
                        Uri uri = Uri.fromFile(file);
                        long date = cursor.getInt(idxDate) * 1000L;

                        if (date < lastDate) {
                            // Date is before the most recent
                            Log.i(TAG, "Screenshot date " + new Date(date) + " is before " + new Date(lastDate));

                            // Likely a deletion
                            ScreenShotNotifications.getInstance(context)
                                    .delete(uri);

                        } else if (file.exists()) {
                            lastDate = date;
                            onScreenShotListener.onScreenShot(uri);
                        }
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

    }

}
