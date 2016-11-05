package com.ToxicBakery.app.screenshot_redaction.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.ScreenshotApplication;
import com.ToxicBakery.app.screenshot_redaction.notification.ScreenShotNotifications;

public class DeleteReceiver extends BroadcastReceiver {

    private static final String ACTION_DELETE = "com.ToxicBakery.app.screenshot_redaction.receiver.DeleteReceiver.ACTION_DELETE";
    private static final String EXTRA_OCR_URI = "EXTRA_OCR_URI";

    public static Intent createDeleteIntent(@NonNull Context context,
                                            @NonNull Uri uri) {

        final Intent intent = new Intent(context, DeleteReceiver.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_OCR_URI, uri);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getParcelableExtra(EXTRA_OCR_URI);
        ((ScreenshotApplication) context.getApplicationContext())
                .getScreenShotNotifications()
                .delete(uri);
    }

}
