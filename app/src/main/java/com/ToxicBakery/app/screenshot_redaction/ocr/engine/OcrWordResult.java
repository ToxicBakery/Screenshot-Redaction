package com.ToxicBakery.app.screenshot_redaction.ocr.engine;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class OcrWordResult implements Parcelable {

    private static final String TAG = "OcrWordResult";

    public static final Creator<OcrWordResult> CREATOR = new Creator<OcrWordResult>() {
        @Override
        public OcrWordResult createFromParcel(Parcel in) {
            return new OcrWordResult(in);
        }

        @Override
        public OcrWordResult[] newArray(int size) {
            return new OcrWordResult[size];
        }
    };

    private final String word;

    @FloatRange(from = 0.0, to = 1.0)
    private final double confidence;

    private final Rect boundingBox;

    public OcrWordResult(@NonNull String word,
                         @FloatRange(from = 0.0, to = 1.0) double confidence,
                         @NonNull Rect boundingBox) {

        this.boundingBox = boundingBox;
        this.word = word;
        this.confidence = confidence;
    }

    protected OcrWordResult(Parcel in) {
        word = in.readString();
        confidence = in.readDouble();
        boundingBox = in.readParcelable(Rect.class.getClassLoader());
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public String getWord() {
        return word;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public double getWordConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        try {
            return new JSONObject()
                    .put("word", word)
                    .put("boundingBox", boundingBox)
                    .toString();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to convert to JSON", e);
            return "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeDouble(confidence);
        dest.writeParcelable(boundingBox, flags);
    }

}
