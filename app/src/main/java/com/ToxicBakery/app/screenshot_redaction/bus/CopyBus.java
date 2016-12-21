package com.ToxicBakery.app.screenshot_redaction.bus;

import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard.ICopyConfiguration;

public class CopyBus extends ADefaultBus<ICopyConfiguration> {

    private static volatile CopyBus instance;

    public static CopyBus getInstance() {
        if (instance == null) {
            synchronized (CopyBus.class) {
                if (instance == null) {
                    instance = new CopyBus();
                }
            }
        }

        return instance;
    }

}
