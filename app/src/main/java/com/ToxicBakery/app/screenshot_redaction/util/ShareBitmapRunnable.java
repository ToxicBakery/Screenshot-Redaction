package com.ToxicBakery.app.screenshot_redaction.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.R;

import java.io.IOException;

public class ShareBitmapRunnable implements Runnable {

    private static final String TAG = "ShareBitmapRunnable";
    private static final String REDACTIONS_DIRECTORY = "redactions";

    final Context context;
    final Bitmap bitmap;

    public ShareBitmapRunnable(@NonNull Context context,
                               @NonNull Bitmap bitmap) {

        this.context = context.getApplicationContext();
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        try {
            Uri uri = new BitmapFileUtil(context, REDACTIONS_DIRECTORY)
                    .save(bitmap);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setType("image/png");

            Intent chooser = Intent.createChooser(share, context.getString(R.string.share_title));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
        }
    }

}
