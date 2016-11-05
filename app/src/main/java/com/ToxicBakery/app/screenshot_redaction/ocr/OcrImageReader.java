package com.ToxicBakery.app.screenshot_redaction.ocr;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.ToxicBakery.android.version.Is;
import com.ToxicBakery.app.screenshot_redaction.ScreenshotApplication;
import com.ToxicBakery.app.screenshot_redaction.notification.ScreenShotNotifications;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.IOcrEngine;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.IOcrEngine.IOcrProgress;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.TessTwoOcrEngine;
import com.ToxicBakery.app.screenshot_redaction.util.PermissionCheck;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.ToxicBakery.android.version.SdkVersion.MARSHMALLOW;

@SuppressWarnings("WeakerAccess")
public class OcrImageReader {

    private static final String TAG = "OcrImageReader";

    @WorkerThread
    static Bitmap uriToBitmap(@NonNull Context context,
                              @NonNull Uri uri) throws IOException {

        InputStream inputStream = context.getContentResolver()
                .openInputStream(uri);

        try {
            return BitmapFactory.decodeStream(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public void submit(@NonNull Context context,
                       @NonNull Uri uri) {

        OcrImageWorker ocrImageWorker = new OcrImageWorker(context);

        Observable.just(uri)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(ocrImageWorker);
    }

    static class OcrImageWorker implements Action1<Uri> {

        private static final String[] REQUIRED_PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        private final Context context;

        OcrImageWorker(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public void call(Uri uri) {

            if (Is.greaterThanOrEqual(MARSHMALLOW) && !PermissionCheck.hasPermissions(context, REQUIRED_PERMISSIONS)) {
                Log.w(TAG, "Failed to perform OCR. Missing required permissions.");
                return;
            }

            final long startTime = System.currentTimeMillis();

            IOcrEngine engine = createEngine();
            Bitmap bitmap;

            try {
                bitmap = uriToBitmap(context, uri);
                Log.d(TAG, "Screenshot dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            } catch (IOException e) {
                Log.e(TAG, "Screenshot not found " + uri, e);
                return;
            }

            try {
                // Initialize the engine
                engine.init(context);

                // Sync updates
                ScreenshotApplication screenshotApplication = (ScreenshotApplication) context.getApplicationContext();
                ScreenShotNotifications screenShotNotifications = screenshotApplication.getScreenShotNotifications();
                IOcrProgress ocrProgressCallback = new OcrProgressImpl(context, screenShotNotifications, uri);
                Collection<OcrWordResult> boundingBoxes = engine.getWordResults(bitmap, ocrProgressCallback);

                // Clean up
                engine.recycle();

                // Create and store the result
                OcrImageResult ocrImageResult = new OcrImageResult(uri, boundingBoxes);
                OcrImageResultStore.getInstance()
                        .storeResultAndNotify(ocrImageResult);

                Log.d(TAG, "Found " + boundingBoxes.size() + " results.");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception occurred.", e);
            } finally {
                Log.d(TAG, "Completed in ~" + (System.currentTimeMillis() - startTime));
            }
        }

        /**
         * Get a configured OCR engine instance.
         * <p>
         * The returned instance is not initialized.
         *
         * @return a engine configured for locating word bounding boxes in bitmaps
         */
        IOcrEngine createEngine() {
            // This can be used to pick different OCR engines and configurations
            return new TessTwoOcrEngine();
        }

    }

    static class OcrProgressImpl implements IOcrProgress {

        private final Context context;
        private final ScreenShotNotifications screenShotNotifications;
        private final Uri uri;

        private int lastUpdate = -1;

        OcrProgressImpl(@NonNull Context context,
                        @NonNull ScreenShotNotifications screenShotNotifications,
                        @NonNull Uri uri) {

            this.context = context.getApplicationContext();
            this.screenShotNotifications = screenShotNotifications;
            this.uri = uri;
        }

        @Override
        public void onProgress(@IntRange(from = 0, to = 100) int progress) {
            // Prevent spamming changes
            if (progress > lastUpdate) {
                lastUpdate = progress;
                screenShotNotifications.update(uri, progress);
            }
        }

    }

}
