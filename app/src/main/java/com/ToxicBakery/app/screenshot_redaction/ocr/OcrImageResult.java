package com.ToxicBakery.app.screenshot_redaction.ocr;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;

import java.util.Collection;
import java.util.Collections;

public class OcrImageResult {

    private final Collection<OcrWordResult> wordResults;
    private final Uri uri;

    OcrImageResult(@NonNull Uri uri,
                   @NonNull Collection<OcrWordResult> boundingBoxes) {

        this.wordResults = Collections.unmodifiableCollection(boundingBoxes);
        this.uri = uri;
    }

    /**
     * Image uri processed by OCR.
     *
     * @return content resolvable image uri
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * A unmodifiable list of bounding boxes. Each bounding box is a word location
     *
     * @return bounding boxes of found words
     */
    public Collection<OcrWordResult> getWordResults() {
        return wordResults;
    }

}
