package com.ToxicBakery.app.screenshot_redaction.ocr.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.ToxicBakery.app.screenshot_redaction.BuildConfig;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;
import com.googlecode.tesseract.android.TessBaseAPI.PageSegMode;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

public class TessTwoOcrEngine implements IOcrEngine, TessBaseAPI.ProgressNotifier {

    private final TessBaseAPI tessBaseAPI;
    private final Object progressLock;

    private volatile boolean isInitialized;
    private volatile IOcrProgress progress;

    public TessTwoOcrEngine() {
        tessBaseAPI = new TessBaseAPI(this);
        progressLock = new Object();
    }

    @Override
    public void init(@NonNull Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);

        if (externalFilesDir == null) {
            throw new RuntimeException("Missing required training data");
        }

        tessBaseAPI.setDebug(BuildConfig.DEBUG);
        tessBaseAPI.init(externalFilesDir.getAbsolutePath(), "eng");
        tessBaseAPI.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
        isInitialized = true;
    }

    @WorkerThread
    @NonNull
    @Override
    public Collection<OcrWordResult> getWordResults(@NonNull Bitmap bitmap,
                                                    @NonNull IOcrProgress progressCallback) throws OcrEngineException {

        // Tess-two only has one callback for progress so multiple threads is not feasible as
        // knowing the progress source can not be determined. It should be possible to bring up
        // multiple Tesseract instances to achieve multiple callbacks but that would be rather
        // resource intensive.
        synchronized (tessBaseAPI) {

            synchronized (progressLock) {
                this.progress = progressCallback;
            }

            if (!isInitialized) {
                throw new OcrEngineException("Engine used before call to init()");
            }

            final Collection<OcrWordResult> wordResults = new LinkedList<>();
            ResultIterator resultIterator = null;

            try {
                tessBaseAPI.setImage(bitmap);

                // This must be called before attempting to get results :sigh:
                tessBaseAPI.getUTF8Text();

                // Get the results we actually want
                resultIterator = tessBaseAPI.getResultIterator();
                resultIterator.begin();

                do {
                    String word = resultIterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
                    Rect rect = resultIterator.getBoundingRect(PageIteratorLevel.RIL_WORD);
                    double confidence = resultIterator.confidence(PageIteratorLevel.RIL_WORD);

                    if (word == null
                            || !rect.intersects(0, 0, bitmap.getWidth(), bitmap.getHeight())) {

                        // No results return invalid rect, check for this case and continue
                        continue;
                    }

                    OcrWordResult ocrWordResult = new OcrWordResult(word, confidence, rect);
                    wordResults.add(ocrWordResult);

                } while (resultIterator.next(PageIteratorLevel.RIL_WORD));
            } catch (Exception e) {
                throw new OcrEngineException(e);
            } finally {
                recycleQuietly(resultIterator);
            }

            return wordResults;
        }
    }

    @Override
    public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
        synchronized (progressLock) {
            if (progress != null) {
                progress.onProgress(progressValues.getPercent());
            }
        }
    }

    @Override
    public void recycle() {
        if (isInitialized) {
            isInitialized = false;
            tessBaseAPI.end();
        }
    }

    void recycleQuietly(@Nullable ResultIterator resultIterator) {
        if (resultIterator != null) {
            try {
                resultIterator.delete();
            } catch (Exception ignored) {
            }
        }
    }

}
