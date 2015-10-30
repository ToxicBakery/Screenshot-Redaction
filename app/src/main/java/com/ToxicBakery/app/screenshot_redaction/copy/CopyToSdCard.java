package com.ToxicBakery.app.screenshot_redaction.copy;

import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.greenrobot.event.EventBus;
import jonathanfinerty.once.Once;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static jonathanfinerty.once.Once.THIS_APP_VERSION;

public class CopyToSdCard {

    private static final String TAG = "CopyToSdCard";

    private static final EventBus COPY_BUS = EventBus.builder()
            .logNoSubscriberMessages(true)
            .logSubscriberExceptions(true)
            .throwSubscriberException(true)
            .build();

    public static EventBus getEventBus() {
        return COPY_BUS;
    }

    public static void copy(@NonNull ICopyConfiguration copy) {
        Observable.just(copy)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new CopyAction());
    }

    public interface ICopyConfiguration {

        InputStream getCopyStream() throws IOException;

        File getTarget() throws IOException;

        long getSize() throws IOException;

    }

    private static class CopyAction implements Action1<ICopyConfiguration> {

        @Override
        public void call(ICopyConfiguration copyConfig) {
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

                COPY_BUS.post(copyConfig);
            }
        }
    }

}
