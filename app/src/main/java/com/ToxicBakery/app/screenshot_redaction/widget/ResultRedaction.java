package com.ToxicBakery.app.screenshot_redaction.widget;

import android.os.Parcel;
import android.os.Parcelable;

import com.ToxicBakery.app.screenshot_redaction.ocr.engine.OcrWordResult;

public class ResultRedaction implements Parcelable {

    public static final Creator<ResultRedaction> CREATOR = new Creator<ResultRedaction>() {
        @Override
        public ResultRedaction createFromParcel(Parcel in) {
            return new ResultRedaction(in);
        }

        @Override
        public ResultRedaction[] newArray(int size) {
            return new ResultRedaction[size];
        }
    };

    private final OcrWordResult wordResult;

    private boolean dictRedacted;
    private boolean isUserToggled;

    public ResultRedaction(OcrWordResult wordResult) {
        this.wordResult = wordResult;
    }

    public ResultRedaction(OcrWordResult wordResult, boolean dictRedacted) {
        this.wordResult = wordResult;
        this.dictRedacted = dictRedacted;
    }

    protected ResultRedaction(Parcel in) {
        wordResult = in.readParcelable(OcrWordResult.class.getClassLoader());
        dictRedacted = in.readByte() != 0;
        isUserToggled = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(wordResult, flags);
        dest.writeByte((byte) (dictRedacted ? 1 : 0));
        dest.writeByte((byte) (isUserToggled ? 1 : 0));
    }

    public boolean isDictRedacted() {
        return dictRedacted;
    }

    public void isDictRedacted(boolean dictRedacted) {
        this.dictRedacted = dictRedacted;
    }

    public OcrWordResult getWordResult() {
        return wordResult;
    }

    public void userToggled() {
        isUserToggled = !isUserToggled;
    }

    public boolean isUserToggled() {
        return isUserToggled;
    }

}
