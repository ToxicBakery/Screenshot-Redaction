package com.ToxicBakery.app.screenshot_redaction.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

public class UriObserver {

    private static final String[] ALL_FIELDS = new String[]{"*"};

    private final Cursor cursor;
    private final ContentObserver contentObserver;

    /**
     * Attempt to observe a Uri for changes.
     *
     * @param context         application context
     * @param uri             to be observed for changes
     * @param contentObserver change listener for the cursor
     * @throws ObserverException
     */
    public UriObserver(@NonNull Context context,
                       @NonNull Uri uri,
                       @NonNull ContentObserver contentObserver) throws ObserverException {

        ContentResolver contentResolver = context.getContentResolver();
        cursor = contentResolver.query(uri, ALL_FIELDS, null, null, null);
        this.contentObserver = contentObserver;

        if (cursor == null) {
            throw new ObserverException("Unable to observer on Uri: " + uri);
        }

        cursor.registerContentObserver(contentObserver);
    }

    /**
     * Stop listening to the Uri.
     */
    public void stop() {
        cursor.unregisterContentObserver(contentObserver);
    }

    public static final class ObserverException extends Exception {

        ObserverException(String message) {
            super(message);
        }

    }

}
