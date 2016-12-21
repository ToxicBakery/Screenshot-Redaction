package com.ToxicBakery.app.screenshot_redaction.ocr;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ToxicBakery.app.screenshot_redaction.bus.OcrImageResultBus;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class OcrImageResultStore {

    private static OcrImageResultStore instance;

    private final Map<Uri, OcrImageResult> results;
    private final OcrImageResultBus ocrImageResultBus;

    OcrImageResultStore() {
        results = new HashMap<>();
        ocrImageResultBus = OcrImageResultBus.getInstance();
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
        ocrImageResultBus.post(ocrImageResult);
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
