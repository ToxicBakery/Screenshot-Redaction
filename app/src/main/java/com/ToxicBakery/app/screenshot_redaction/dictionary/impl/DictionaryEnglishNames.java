package com.ToxicBakery.app.screenshot_redaction.dictionary.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard.ICopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.copy.WordListRawResourceCopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionary;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class DictionaryEnglishNames implements IDictionary {

    private static final String TAG = "DictionaryEnglishNames";

    private static DictionaryEnglishNames instance;

    final ICopyConfiguration copyConfiguration;

    private Set<String> words;

    DictionaryEnglishNames(@NonNull Context context) {
        CopyToSdCard.getEventBus()
                .register(this);

        copyConfiguration = new WordListRawResourceCopyConfiguration(context, R.raw.wordlist_english_names);
        CopyToSdCard.copy(copyConfiguration);
    }

    public static DictionaryEnglishNames getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (DictionaryEnglishNames.class) {
                if (instance == null) {
                    instance = new DictionaryEnglishNames(context);
                }
            }
        }

        return instance;
    }

    @Override
    public boolean shouldRedact(@NonNull String word) {
        // TODO Should rules be configurable?
        if (words == null) {
            throw new IllegalStateException("Dictionary not initialized.");
        } else if (word.length() <= 3) {
            // Word less than or equal to 3 characters (likely safe)
            return false;
        } else {
            // Lowercase then strip marks common in sentences and words
            word = word.toLowerCase(Locale.ENGLISH)
                    .replaceAll("[^a-z0-9\\-\\._!?']", "");

            // If words are not loaded, redact everything otherwise redact known names
            return words.contains(word);
        }
    }

    @Override
    public int getName() {
        return R.string.dict_en_names;
    }

    @NonNull
    @Override
    public String getUUID() {
        return "be401e34-e046-46cf-8958-59efedb7d260";
    }

    @SuppressWarnings("unused")
    public void onEvent(WordListRawResourceCopyConfiguration copyConfiguration) {
        if (this.copyConfiguration.equals(copyConfiguration)) {
            try {
                DB db = DBMaker.fileDB(copyConfiguration.getTarget())
                        .closeOnJvmShutdown()
                        .transactionDisable()
                        .readOnly()
                        .make();

                words = db.hashSet("wordlist");

                CopyToSdCard.getEventBus()
                        .unregister(this);

            } catch (IOException e) {
                Log.e(TAG, "Failed to open copied database", e);
            }
        }
    }

}
