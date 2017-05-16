package com.ToxicBakery.app.screenshot_redaction.dictionary.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.bus.CopyBus;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard.ICopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.copy.WordListRawResourceCopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionary;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DictionaryEnglishNames implements IDictionary {

    private static final String TAG = "DictionaryEnglishNames";

    private static DictionaryEnglishNames instance;

    @VisibleForTesting
    final ICopyConfiguration copyConfiguration;

    private final CopyBus copyBus;

    private Set<String> words;

    DictionaryEnglishNames(@NonNull Context context) {
        copyConfiguration = new WordListRawResourceCopyConfiguration(context, R.raw.wordlist_english_names);
        copyBus = CopyBus.getInstance();
        listenCopy();
        new CopyToSdCard().copy(copyConfiguration);
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

    private void listenCopy() {
        copyBus.register()
                .filter(new Func1<ICopyConfiguration, Boolean>() {
                    @Override
                    public Boolean call(ICopyConfiguration copyConfiguration) {
                        return copyConfiguration == DictionaryEnglishNames.this.copyConfiguration;
                    }
                })
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ICopyConfiguration>() {
                    @Override
                    public void call(ICopyConfiguration iCopyConfiguration) {
                        try {
                            DB db = DBMaker.fileDB(copyConfiguration.getTarget())
                                    .closeOnJvmShutdown()
                                    .transactionDisable()
                                    .readOnly()
                                    .make();

                            words = db.hashSet(getUUID());
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to open copied database", e);
                        }
                    }
                });
    }

}
