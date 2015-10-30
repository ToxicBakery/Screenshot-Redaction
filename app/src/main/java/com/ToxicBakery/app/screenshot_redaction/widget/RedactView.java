package com.ToxicBakery.app.screenshot_redaction.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.ToxicBakery.app.screenshot_redaction.R;

import java.util.LinkedHashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class RedactView extends PhotoView implements PhotoViewAttacher.OnMatrixChangedListener, PhotoViewAttacher.OnPhotoTapListener {

    private static final String TAG = "RedactView";

    private final Map<String, ResultRedaction> resultRedactionMap;
    private final Paint paintRedactDict;
    private final Paint paintRedactNone;
    private final Paint paintRedactUser;
    private final Paint paintRedactExport;
    private final Matrix redactionMatrix;
    private final Matrix redactionMatrixInverse;
    private final RectF destRect;
    private final RectF displayRect;
    private final float[] touchPoint;

    public RedactView(Context context) {
        this(context, null);
    }

    public RedactView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public RedactView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);

        resultRedactionMap = new LinkedHashMap<>();
        destRect = new RectF();
        displayRect = new RectF();
        redactionMatrix = new Matrix();
        redactionMatrixInverse = new Matrix();
        touchPoint = new float[2];

        paintRedactDict = new Paint();
        paintRedactDict.setStrokeWidth(dpToPixels(context, 3));
        paintRedactDict.setColor(ContextCompat.getColor(getContext(), R.color.redact_dict));
        paintRedactDict.setStyle(Style.FILL);

        paintRedactNone = new Paint(paintRedactDict);
        paintRedactNone.setColor(ContextCompat.getColor(getContext(), R.color.redact_none));
        paintRedactNone.setStyle(Style.STROKE);

        paintRedactUser = new Paint(paintRedactDict);
        paintRedactUser.setColor(ContextCompat.getColor(getContext(), R.color.redact_user));

        paintRedactExport = new Paint(paintRedactDict);

        setOnMatrixChangeListener(this);
        setOnPhotoTapListener(this);
    }

    static float dpToPixels(@NonNull Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @MainThread
    public void addResultRedaction(@NonNull ResultRedaction resultRedaction) {
        String word = resultRedaction.getWordResult()
                .getWord();

        resultRedactionMap.put(word, resultRedaction);
    }

    @Override
    public void onMatrixChanged(RectF rect) {
        // TODO Update to `void getDisplayMatrix(matrix)` once https://github.com/chrisbanes/PhotoView/pull/304 is accepted.
        redactionMatrix.set(getDisplayMatrix());
        redactionMatrix.invert(redactionMatrixInverse);

        // TODO Open PR for `void getDisplayRect(rect)`
        displayRect.set(getDisplayRect());
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        touchPoint[0] = (x * displayRect.width()) + displayRect.left;
        touchPoint[1] = (y * displayRect.height()) + displayRect.top;
        redactionMatrixInverse.mapPoints(touchPoint);

        int dX = (int) touchPoint[0];
        int dY = (int) touchPoint[1];

        for (ResultRedaction resultRedaction : resultRedactionMap.values()) {
            Rect rect = resultRedaction.getWordResult()
                    .getBoundingBox();

            if (rect.contains(dX, dY)) {

                // Toggle the user redaction choice and request a draw
                resultRedaction.userToggled();
                invalidate();
                break;
            }
        }

    }

    @NonNull
    public Bitmap toBitmap() {
        displayRect.width();
        Bitmap source = getImageBitmap();
        if (source == null) {
            throw new IllegalStateException("Attempt to convert non configured view to bitmap.");
        }

        Bitmap bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(source, 0, 0, new Paint());

        // Draw redactions on top
        for (ResultRedaction redaction : resultRedactionMap.values()) {
            if (redaction.isDictRedacted()
                    || redaction.isUserToggled()) {
                paintExportRedaction(canvas, redaction);
            }
        }

        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw redactions on top
        for (ResultRedaction redaction : resultRedactionMap.values()) {
            paintRedactionWithMatrix(canvas, redaction);
        }
    }

    @Nullable
    Bitmap getImageBitmap() {
        Drawable drawable = getDrawable();
        return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
    }

    void paintRedactionWithMatrix(Canvas canvas, ResultRedaction redaction) {
        Rect boundingBox = redaction.getWordResult()
                .getBoundingBox();

        destRect.set(boundingBox);

        redactionMatrix.mapRect(destRect);

        Paint redactionPaint = getRedactionPaint(redaction);

        canvas.drawRect(destRect, redactionPaint);
    }

    void paintExportRedaction(Canvas canvas, ResultRedaction redaction) {
        Rect rect = redaction.getWordResult()
                .getBoundingBox();

        canvas.drawRect(rect, paintRedactExport);
    }

    Paint getRedactionPaint(ResultRedaction redaction) {
        boolean userToggled = redaction.isUserToggled();
        boolean dictRedacted = redaction.isDictRedacted();

        if (dictRedacted && !userToggled) {
            // Dictionary redacted word, user did not override
            return paintRedactDict;
        } else if (!dictRedacted && userToggled) {
            // Dictionary did not dictRedacted word, user override
            return paintRedactUser;
        } else {
            // Word is not redacted, user did not override
            return paintRedactNone;
        }
    }

}
