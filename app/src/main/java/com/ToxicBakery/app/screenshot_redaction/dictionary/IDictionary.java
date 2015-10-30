package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface IDictionary {

    /**
     * Determine if a word should be redacted.
     *
     * @param word the word in question
     * @return true if the word should be redacted
     */
    boolean shouldRedact(@NonNull String word);

    /**
     * The name of the dictionary.
     *
     * @return a string resource representation of the dictionary name.
     */
    @StringRes
    int getName();

    /**
     * A constant UUID of the dictionary.
     *
     * @return a constant UUID
     */
    @NonNull
    String getUUID();

}
