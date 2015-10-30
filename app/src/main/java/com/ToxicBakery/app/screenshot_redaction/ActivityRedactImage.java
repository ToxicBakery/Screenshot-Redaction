package com.ToxicBakery.app.screenshot_redaction;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ToxicBakery.app.screenshot_redaction.dictionary.DictionaryProvider;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionary;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionaryStatus;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResult;
import com.ToxicBakery.app.screenshot_redaction.ocr.OcrImageResultStore;
import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;
import com.ToxicBakery.app.screenshot_redaction.util.ShareBitmapRunnable;
import com.ToxicBakery.app.screenshot_redaction.widget.RedactView;
import com.ToxicBakery.app.screenshot_redaction.widget.ResultRedaction;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.view.View.GONE;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ActivityRedactImage extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ActivityRedactImage";
    private static final String EXTRA_OCR_URI = "EXTRA_OCR_URI";

    private Uri uri;
    private OcrImageResult imageResult;
    private View progress;
    private View actionsBar;
    private RedactView redactView;
    private View actionShare;
    private RedactTarget redactTarget;
    private Subscription subscriptionRedactions;
    private Subscription subscriptionDictionaries;

    public static Intent createRedactIntent(@NonNull Context context,
                                            @NonNull Uri uri) {

        final Intent intent = new Intent(context, ActivityRedactImage.class);
        intent.putExtra(EXTRA_OCR_URI, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redact_image);

        uri = getIntent().getParcelableExtra(EXTRA_OCR_URI);

        if (uri == null) {
            throw new IllegalStateException("Missing required Uri.");
        }

        Log.d(TAG, "Received redaction image " + uri);

        progress = findViewById(R.id.progress);
        redactView = (RedactView) findViewById(R.id.redact_view);
        actionsBar = findViewById(R.id.actions);

        actionsBar.setVisibility(GONE);

        actionShare = findViewById(R.id.action_share);

        redactTarget = new RedactTarget();
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
    protected void onResume() {
        super.onResume();

        imageResult = OcrImageResultStore.getInstance()
                .takeResult(uri);

        actionShare.setOnClickListener(this);

        Picasso picasso = Picasso.with(getApplicationContext());
        picasso.invalidate(uri);
        picasso.load(uri)
                .into(redactTarget);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (subscriptionRedactions != null
                && !subscriptionRedactions.isUnsubscribed()) {

            subscriptionRedactions.unsubscribe();
        }

        Picasso.with(getApplicationContext())
                .cancelRequest(redactTarget);

        OcrImageResultStore.getInstance()
                .storeResult(imageResult);
    }

    @FloatRange(from = 0.0, to = 1.0)
    double getConfidenceMin() {
        // FIXME Confidence should be configurable
        return 0.7d;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_share:
                shareBitmap();
                break;
        }
    }

    void shareBitmap() {
        Bitmap bitmap = redactView.toBitmap();

        // Disable future clicks from doing to prevent multiple processing. Re-enabled in onResume
        actionShare.setOnClickListener(null);

        Runnable shareTask = new ShareBitmapRunnable(this, bitmap);
        new Thread(shareTask).start();
    }

    void updateRedactions() {
        if (imageResult == null) {
            Log.e(TAG, "No image results!");
            return;
        }

        subscriptionDictionaries = DictionaryProvider.getInstance(getApplicationContext())
                .getDictionaries()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<IDictionaryStatus>() {

                    final List<IDictionary> dictionaries = new LinkedList<>();

                    @Override
                    public void onCompleted() {
                        processRedactions(dictionaries);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(IDictionaryStatus dictionaryStatus) {
                        if (dictionaryStatus.isEnabled()) {
                            dictionaries.add(dictionaryStatus.getDictionary());
                        }
                    }
                });
    }

    void processRedactions(final List<IDictionary> dictionaries) {
        subscriptionRedactions = Observable.from(imageResult.getWordResults())
                .map(new Func1<OcrWordResult, ResultRedaction>() {

                    @Override
                    public ResultRedaction call(OcrWordResult wordResult) {

                        boolean isDictionaryRedacted = false;
                        if (wordResult.getWordConfidence() > getConfidenceMin()) {

                            for (IDictionary dictionary : dictionaries) {
                                if (dictionary.shouldRedact(wordResult.getWord())) {
                                    isDictionaryRedacted = true;
                                    break;
                                }
                            }
                        }

                        return new ResultRedaction(wordResult, isDictionaryRedacted);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultRedaction>() {
                    @Override
                    public void onCompleted() {
                        redactView.invalidate();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error updated redactions", e);
                    }

                    @Override
                    public void onNext(ResultRedaction redaction) {
                        redactView.addResultRedaction(redaction);
                    }
                });
    }

    private class RedactTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            redactView.setImageBitmap(bitmap);
            actionsBar.setVisibility(View.VISIBLE);
            progress.setVisibility(GONE);

            updateRedactions();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            new AlertDialog.Builder(ActivityRedactImage.this)
                    .setTitle(R.string.error_missing_screenshot_title)
                    .setMessage(R.string.error_missing_screenshot_msessage)
                    .show();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d(TAG, "Preparing to display redaction results.");
        }

    }

}
