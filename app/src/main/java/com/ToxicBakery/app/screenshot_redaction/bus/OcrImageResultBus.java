package com.ToxicBakery.app.screenshot_redaction.bus;

import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResult;

public class OcrImageResultBus extends ADefaultBus<OcrImageResult> {

    private static volatile OcrImageResultBus instance;

    public static OcrImageResultBus getInstance() {
        if (instance == null) {
            synchronized (TutorialBus.class) {
                if (instance == null) {
                    instance = new OcrImageResultBus();
                }
            }
        }

        return instance;
    }

}
