package com.ToxicBakery.app.screenshot_redaction.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapFileUtil {

    private static final String AUTHORITY = "com.toxicbakery.app.screenshot_redaction.FileProvider";

    private final Context context;
    private final String directoryName;

    public BitmapFileUtil(@NonNull Context context,
                          @NonNull String directoryName) {

        this.context = context.getApplicationContext();
        this.directoryName = directoryName;
    }

    public Uri save(@NonNull Bitmap bitmap) throws IOException {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ssZ", Locale.ENGLISH).format(new Date());
        String fileName = String.format(Locale.ENGLISH, "redaction_%s.png", formattedDate);
        File target = new File(getDirectory(), fileName);

        writeToFile(bitmap, target);

        return FileProvider.getUriForFile(context, AUTHORITY, target);
    }

    void writeToFile(@NonNull Bitmap bitmap,
                     @NonNull File target) throws IOException {

        OutputStream outputStream = new FileOutputStream(target);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.close();
    }

    File getDirectory() throws IOException {
        File directory = new File(context.getFilesDir(), directoryName);
        if (!directory.exists()
                && !directory.mkdirs()) {

            throw new IOException("Failed to create storage directory: " + directory.getAbsolutePath());
        }

        return directory;
    }

}
