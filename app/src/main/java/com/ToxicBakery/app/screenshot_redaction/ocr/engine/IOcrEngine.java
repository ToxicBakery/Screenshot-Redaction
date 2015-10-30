package com.ToxicBakery.app.screenshot_redaction.ocr.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Collection;

public interface IOcrEngine {

    /**
     * Initialize the engine and any resources needed to process bounding box requests.
     *
     * @param context application context
     */
    void init(@NonNull Context context);

    /**
     * Process a bitmap for words. Return all found words as bounding boxes of the word.
     * <p>
     * This may be called from multiple threads.
     *
     * @param bitmap image to analyze
     * @param progress progress callback
     * @return bounding boxes of words
     * @throws OcrEngineException wrapping any failures
     */
    @WorkerThread
    @NonNull
    Collection<OcrWordResult> getWordResults(@NonNull Bitmap bitmap,
                                             @NonNull IOcrProgress progress) throws OcrEngineException;

    /**
     * Free an allocated resources. The engine instance will no longer be used.
     */
    void recycle();

    @SuppressWarnings("unused")
    final class OcrEngineException extends Exception {

        public OcrEngineException(String detailMessage) {
            super(detailMessage);
        }

        public OcrEngineException(Throwable throwable) {
            super(throwable);
        }

        public OcrEngineException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

    }

    interface IOcrProgress {

        void onProgress(@IntRange(from = 0, to = 100) int progress);

    }

}
