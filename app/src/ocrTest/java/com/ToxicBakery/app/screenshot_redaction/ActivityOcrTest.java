package com.ToxicBakery.app.screenshot_redaction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;
import com.ToxicBakery.app.screenshot_redaction.util.ShareBitmapRunnable;
import com.ToxicBakery.app.screenshot_redaction.widget.RedactView;
import com.ToxicBakery.app.screenshot_redaction.widget.ResultRedaction;

import java.util.Collection;
import java.util.LinkedList;

import rx.Observable;
import rx.functions.Action1;

public class ActivityOcrTest extends AppCompatActivity implements View.OnClickListener {

    private RedactView redactView;
    private View actionShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redact_image);

        Bitmap bitmap = getDrawableAsBitmap(R.drawable.test_screenshot);
        final Collection<ResultRedaction> resultRedactions = createTestRedactions();

        redactView = (RedactView) findViewById(R.id.redact_view);
        redactView.setImageBitmap(bitmap);

        Observable.from(resultRedactions)
                .subscribe(new Action1<ResultRedaction>() {
                    @Override
                    public void call(ResultRedaction redaction) {
                        redactView.addResultRedaction(redaction);
                    }
                });

        findViewById(R.id.progress)
                .setVisibility(View.GONE);

        actionShare = findViewById(R.id.action_share);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_share:
                shareBitmap();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        actionShare.setOnClickListener(this);
    }

    void shareBitmap() {
        Bitmap bitmap = redactView.toBitmap();

        // Disable future clicks from doing to prevent multiple processing. Re-enabled in onResume
        actionShare.setOnClickListener(null);

        Runnable shareTask = new ShareBitmapRunnable(this, bitmap);
        new Thread(shareTask).start();
    }

    Collection<ResultRedaction> createTestRedactions() {
        Collection<ResultRedaction> redactions = new LinkedList<>();

        // Display a box around the 'send feedback' text in the image for testing touch and visuals
        Rect rect = new Rect(40, 1165, 884, 1253);
        OcrWordResult ocrWordResult = new OcrWordResult("Send feedback about this device", 1, rect);
        ResultRedaction redaction = new ResultRedaction(ocrWordResult, true);

        redactions.add(redaction);

        return redactions;
    }

    Bitmap getDrawableAsBitmap(@DrawableRes int drawableRes) {
        return BitmapFactory.decodeResource(getResources(), drawableRes);
    }

}
