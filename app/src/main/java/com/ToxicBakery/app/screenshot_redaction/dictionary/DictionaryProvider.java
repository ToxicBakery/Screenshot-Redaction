package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglish;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglishNames;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Set;

import rx.Observable;
import rx.functions.Func1;

public class DictionaryProvider {

    private static final String DB_NAME = "DictionaryProvider.mapdb";

    private static DictionaryProvider instance;

    private final NamedDictionary[] dictionaries;
    private final Set<String> enabledDictionaries;

    private DictionaryProvider(@NonNull Context context) {
        dictionaries = new NamedDictionary[]{
                new NamedDictionary(context, DictionaryEnglish.getInstance(context)),
                new NamedDictionary(context, DictionaryEnglishNames.getInstance(context)),
        };

        File dbFile = new File(context.getFilesDir(), DB_NAME);
        DB db = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown()
                .asyncWriteEnable()
                .cacheLRUEnable()
                .make();

        boolean exists = db.exists(DB_NAME);
        enabledDictionaries = db.hashSet(DB_NAME);

        if (!exists) {
            for (NamedDictionary namedDictionary : dictionaries) {
                enabledDictionaries.add(namedDictionary.getDictionary().getUUID());
            }
        }
    }

    public static DictionaryProvider getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (DictionaryProvider.class) {
                if (instance == null) {
                    instance = new DictionaryProvider(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    public Observable<Boolean> setDictionaryEnabled(@NonNull String uuid, boolean enabled) {
        return Observable.just(enabled
                ? enabledDictionaries.add(uuid)
                : enabledDictionaries.remove(uuid));
    }

    public Observable<IDictionaryStatus> getDictionaries() {
        return Observable.from(dictionaries)
                .map(new Func1<NamedDictionary, IDictionaryStatus>() {
                    @Override
                    public IDictionaryStatus call(NamedDictionary namedDictionary) {
                        String uuid = namedDictionary.getDictionary().getUUID();
                        boolean enabled = enabledDictionaries.contains(uuid);
                        return new DictionaryStatus(namedDictionary, enabled);
                    }
                });
    }

}
