package com.ToxicBakery.app.screenshot_redaction.bus;

public class TutorialBus extends ADefaultBus<Integer> {

    private static volatile TutorialBus instance;

    public static TutorialBus getInstance() {
        if (instance == null) {
            synchronized (TutorialBus.class) {
                if (instance == null) {
                    instance = new TutorialBus();
                }
            }
        }

        return instance;
    }

}
