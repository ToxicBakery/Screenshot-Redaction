package com.ToxicBakery.app.screenshot_redaction.notification;

import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.NotificationCompat;

import com.ToxicBakery.app.screenshot_redaction.bus.OcrImageResultBus;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResult;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultPublic;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultStore;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class ScreenShotNotificationsTest {

    private ScreenShotNotifications screenShotNotifications;

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        screenShotNotifications = new ScreenShotNotifications(getContext());
    }

    @Test
    public void testUpdate() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        for (int i = 0; i <= 100; i++) {
            screenShotNotifications.update(uri, i);
        }
    }

    @Test
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

    @Test
    public void testOnEventAsync() throws Exception {
        Uri uri = Uri.parse("http://google.com/");
        Collection<OcrWordResult> boundingBoxes = new LinkedList<>();
        OcrImageResult ocrImageResult = new OcrImageResultPublic(uri, boundingBoxes);

        OcrImageResultBus.getInstance()
                .post(ocrImageResult);
    }

    @Test
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

}