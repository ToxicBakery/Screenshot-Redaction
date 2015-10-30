package com.ToxicBakery.app.screenshot_redaction.ocr;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class OcrImageResultStore {

    private static final EventBus OCR_BUS = EventBus.builder()
            .logNoSubscriberMessages(true)
            .logSubscriberExceptions(true)
            .throwSubscriberException(true)
            .build();

    private static OcrImageResultStore instance;

    private final Map<Uri, OcrImageResult> results;

    OcrImageResultStore() {
        results = new HashMap<>();
    }

    public static EventBus getEventBus() {
        return OCR_BUS;
    }

    public static OcrImageResultStore getInstance() {
        if (instance == null) {
            synchronized (OcrImageResultStore.class) {
                if (instance == null) {
                    instance = new OcrImageResultStore();
                }
            }
        }

        return instance;
    }

    /**
     * Stores an ImageResult for later retrieval.
     *
     * @param ocrImageResult to be stored
     */
    public void storeResult(@NonNull OcrImageResult ocrImageResult) {
        synchronized (results) {
            results.put(ocrImageResult.getUri(), ocrImageResult);
        }
    }

    void storeResultAndNotify(@NonNull OcrImageResult ocrImageResult) {
        storeResult(ocrImageResult);
        OCR_BUS.post(ocrImageResult);
    }

    /**
     * Take the results for a specific Uri. This removes the result instance from the store.
     *
     * @param uri the uri of the result to take
     * @return ImageResult for the Uri or null
     */
    @Nullable
    public OcrImageResult takeResult(Uri uri) {
        synchronized (results) {
            return results.remove(uri);
        }
    }

}
