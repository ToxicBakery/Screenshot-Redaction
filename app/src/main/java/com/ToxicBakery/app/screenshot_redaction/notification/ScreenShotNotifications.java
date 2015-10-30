package com.ToxicBakery.app.screenshot_redaction.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.ActivityRedactImage;
import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResult;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultStore;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;
import com.ToxicBakery.app.screenshot_redaction.receiver.DeleteReceiver;

import java.util.Collection;

public class ScreenShotNotifications {

    private static final String TAG = "ScreenShotNotifications";
    private static final int NOTIFICATION_WORKING_ID = 1;

    private static ScreenShotNotifications instance;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    ScreenShotNotifications(@NonNull Context context) {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);

        OcrImageResultStore.getEventBus()
                .register(this);
    }

    public static ScreenShotNotifications getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (ScreenShotNotifications.class) {
                if (instance == null) {
                    instance = new ScreenShotNotifications(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    public void update(@NonNull Uri uri,
                       @IntRange(from = 0, to = 100) int progress) {

        // Delete Intent
        Intent deleteIntent = DeleteReceiver.createDeleteIntent(context, uri);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, uri.hashCode(), deleteIntent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setLocalOnly(true)
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_search))
                .setContentTitle(getString(R.string.notification_title_searching_image))
                .setContentText(getString(R.string.notification_message_search_image))
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_delete, getString(android.R.string.cancel), deletePendingIntent)
                .build();

        notify(uri, notification);
    }

    public void delete(@NonNull Uri uri) {
        Log.d(TAG, "Cancelling notification for " + uri);

        OcrImageResultStore.getInstance()
                .takeResult(uri);

        notificationManager.cancel(uri.toString(), NOTIFICATION_WORKING_ID);
    }

    // Processing complete
    @SuppressWarnings({"unused", "deprecation"})
    public void onEventAsync(OcrImageResult ocrImageResult) {
        Log.d(TAG, "Notifying completion");

        Uri uri = ocrImageResult.getUri();
        Collection<OcrWordResult> wordResults = ocrImageResult.getWordResults();
        int boundingBoxesCount = wordResults.size();

        // TODO Add big picture possibly showing redaction applied to all matches?

        // Redact Intent
        Intent redactIntent = ActivityRedactImage.createRedactIntent(context, uri);
        PendingIntent redactPendingIntent = PendingIntent.getActivity(context, uri.hashCode(), redactIntent, 0);

        // Delete Intent
        Intent deleteIntent = DeleteReceiver.createDeleteIntent(context, uri);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, uri.hashCode(), deleteIntent, 0);

        // Notify the user the screenshot has <boundingBoxesCount> items found in it
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_search))
                .setContentTitle(getString(R.string.notification_title_found_items))
                .setContentText(getString(R.string.notification_message_found_items, boundingBoxesCount))
                .setAutoCancel(true)
                .setLocalOnly(true)
                .setColor(ContextCompat.getColor(context, R.color.primary_dark))
                .setOnlyAlertOnce(true)
                .setContentIntent(redactPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .build();

        // Notify on the main thread (required by notification manager)
        notify(uri, notification);
    }

    void notify(@NonNull Uri uri,
                @NonNull Notification notification) {

        notificationManager.notify(uri.toString(), NOTIFICATION_WORKING_ID, notification);
    }

    @NonNull
    String getString(@StringRes int stringRes) {
        return context.getString(stringRes);
    }

    @NonNull
    String getString(@StringRes int stringRes,
                     @NonNull Object... formatArgs) {

        return context.getString(stringRes, formatArgs);
    }

}
