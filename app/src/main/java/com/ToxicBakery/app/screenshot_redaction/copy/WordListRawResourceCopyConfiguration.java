package com.ToxicBakery.app.screenshot_redaction.copy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class WordListRawResourceCopyConfiguration implements CopyToSdCard.ICopyConfiguration {

    private static final String FOLDER = "wordlists";

    private final Context context;
    private final int rawRes;
    private final File baseDir;

    public WordListRawResourceCopyConfiguration(@NonNull Context context,
                                                @RawRes int rawRes) {

        this.context = context.getApplicationContext();
        this.rawRes = rawRes;

        baseDir = new File(context.getExternalFilesDir(null), FOLDER);

        try {
            FileUtils.forceMkdir(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create required directory " + baseDir, e);
        }
    }

    @Override
    public InputStream getCopyStream() throws IOException {
        return context.getResources()
                .openRawResource(rawRes);
    }

    @Override
    public File getTarget() throws IOException {
        String resourceEntryName = context.getResources()
                .getResourceEntryName(rawRes);

        return new File(baseDir, resourceEntryName);
    }

    @Override
    public long getSize() throws IOException {
        InputStream inputStream = context.getResources()
                .openRawResource(rawRes);

        long fileSize = inputStream.available();
        inputStream.close();

        return fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null
                && o instanceof WordListRawResourceCopyConfiguration) {

            WordListRawResourceCopyConfiguration other = (WordListRawResourceCopyConfiguration) o;
            return rawRes == other.rawRes
                    && baseDir.equals(other.baseDir);
        }

        return false;
    }

}
