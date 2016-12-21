package com.ToxicBakery.app.screenshot_redaction.copy;

import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.bus.CopyBus;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jonathanfinerty.once.Once;
import rx.functions.Action1;

import static jonathanfinerty.once.Once.THIS_APP_VERSION;

class CopyAction implements Action1<CopyToSdCard.ICopyConfiguration> {

    private static final String TAG = "CopyAction";

    private final CopyBus copyBus;

    CopyAction() {
        copyBus = CopyBus.getInstance();
    }

    @Override
    public void call(CopyToSdCard.ICopyConfiguration copyConfig) {
        long startTime = System.currentTimeMillis();
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            // Dumb evaluate source/target on size
            File target = copyConfig.getTarget();
            long originSize = copyConfig.getSize();
            long targetSize = target.exists() ? target.length() : -1;

            if (originSize == targetSize
                    && Once.beenDone(THIS_APP_VERSION, target.getPath())) {

                Log.d(TAG, "Skipping copy of " + target.getName());
            } else {
                Log.d(TAG, "Copying " + target.getName());

                Once.markDone(target.getPath());

                inputStream = copyConfig.getCopyStream();
                outputStream = new FileOutputStream(target);
                IOUtils.copyLarge(inputStream, outputStream);
                outputStream.flush();
            }

            Log.d(TAG, "Completed copying " + target.getName() + " in ~" + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy file.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);

            copyBus.post(copyConfig);
        }
    }
}
