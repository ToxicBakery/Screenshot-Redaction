package com.ToxicBakery.app.screenshot_redaction.ocr.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.IntRange;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.ActivityTest;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TessTwoOcrEngineTest {

    @Rule
    public ActivityTestRule<ActivityTest> activityTestRule = new ActivityTestRule<>(ActivityTest.class);

    private Context getContext() {
        return activityTestRule.getActivity();
    }

    private static Bitmap genBitmap(int width, int height, String text) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24.0f);

        canvas.drawText(text, width / 2, height / 2, paint);

        return bitmap;
    }

    @Before
    public void setUp() throws Exception {
        File tessData = new File(getContext().getExternalFilesDir(null), "tessdata");
        assertTrue(tessData.exists() || tessData.mkdirs());

        File target = new File(tessData, "eng.traineddata");
        InputStream inputStream = getContext().getResources()
                .openRawResource(R.raw.eng);
        OutputStream outputStream = new FileOutputStream(target);
        IOUtils.copyLarge(inputStream, outputStream);
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testGetWordResults() throws Exception {
        Bitmap bitmap = genBitmap(640, 480, "Hello");

        TessTwoOcrEngine tessTwoOcrEngine = new TessTwoOcrEngine();
        tessTwoOcrEngine.init(getContext());
        Collection<OcrWordResult> wordResults = tessTwoOcrEngine.getWordResults(bitmap, new EmptyProgress());
        tessTwoOcrEngine.recycle();

        assertEquals(1, wordResults.size());
        OcrWordResult[] ocrWordResults = wordResults.toArray(new OcrWordResult[1]);

        assertEquals("Hello", ocrWordResults[0].getWord());
        assertTrue(ocrWordResults[0].getWordConfidence() > 0.9d);
    }

    @Test
    public void testBlankImage() throws Exception {
        Bitmap bitmap = genBitmap(640, 480, "");

        TessTwoOcrEngine tessTwoOcrEngine = new TessTwoOcrEngine();
        tessTwoOcrEngine.init(getContext());
        Collection<OcrWordResult> wordResults = tessTwoOcrEngine.getWordResults(bitmap, new EmptyProgress());
        tessTwoOcrEngine.recycle();

        assertEquals(0, wordResults.size());
    }

    static class EmptyProgress implements IOcrEngine.IOcrProgress {

        @Override
        public void onProgress(@IntRange(from = 0, to = 100) int progress) {
        }

    }

}