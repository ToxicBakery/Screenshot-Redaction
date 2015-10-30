package com.ToxicBakery.app.screenshot_redaction.ocr;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;

import java.util.Collection;

public class OcrImageResultPublic extends OcrImageResult {

    public OcrImageResultPublic(@NonNull Uri uri, @NonNull Collection<OcrWordResult> boundingBoxes) {
        super(uri, boundingBoxes);
    }

}
