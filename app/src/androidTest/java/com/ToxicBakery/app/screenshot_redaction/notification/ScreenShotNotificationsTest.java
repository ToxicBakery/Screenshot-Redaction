package com.ToxicBakery.app.screenshot_redaction.notification;

import android.app.Notification;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.test.AndroidTestCase;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResult;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultPublic;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultStore;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;

import java.util.Collection;
import java.util.LinkedList;

public class ScreenShotNotificationsTest extends AndroidTestCase {

    ScreenShotNotifications screenShotNotifications;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        screenShotNotifications = ScreenShotNotifications.getInstance(getContext());
    }

    public void testGetInstance() throws Exception {
        assertEquals(screenShotNotifications, ScreenShotNotifications.getInstance(getContext()));
    }

    public void testUpdate() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        for (int i = 0; i <= 100; i++) {
            screenShotNotifications.update(uri, i);
        }
    }

    public void testDelete() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        Collection<OcrWordResult> boundingBoxes = new LinkedList<>();
        OcrImageResult ocrImageResult = new OcrImageResultPublic(uri, boundingBoxes);

        OcrImageResultStore ocrImageResultStore = OcrImageResultStore.getInstance();

        ocrImageResultStore.storeResult(ocrImageResult);

        screenShotNotifications.delete(uri);

        ocrImageResult = ocrImageResultStore.takeResult(uri);
        assertNull(ocrImageResult);
    }

    public void testOnEventAsync() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        Collection<OcrWordResult> boundingBoxes = new LinkedList<>();
        OcrImageResult ocrImageResult = new OcrImageResultPublic(uri, boundingBoxes);

        OcrImageResultStore.getEventBus()
                .post(ocrImageResult);
    }

    public void testNotify() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        Notification notification = new NotificationCompat.Builder(getContext())
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setContentTitle("Something")
                .setContentText("Unimportant")
                .setLocalOnly(true)
                .setOnlyAlertOnce(true)
                .build();

        screenShotNotifications.notify(uri, notification);
    }

    public void testGetString() throws Exception {
        assertEquals(getContext().getString(R.string.app_name), screenShotNotifications.getString(R.string.app_name));
    }

    public void testGetString1() throws Exception {
        String gen = screenShotNotifications.getString(R.string.notification_message_found_items, 1337);
        assertEquals(getContext().getString(R.string.notification_message_found_items, 1337), gen);
    }

}