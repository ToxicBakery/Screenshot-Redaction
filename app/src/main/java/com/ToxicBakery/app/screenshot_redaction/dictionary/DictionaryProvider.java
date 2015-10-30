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
import rx.Subscriber;

public class DictionaryProvider {

    private static final String DB_NAME = "DictionaryProvider.mapdb";

    static DictionaryProvider instance;

    final NamedDictionary[] dictionaries;
    final Set<String> enabledDictionaries;

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

        final boolean exists = db.exists(DB_NAME);
        enabledDictionaries = db.hashSet(DB_NAME);

        if (!exists) {
            for (NamedDictionary namedDictionary : dictionaries) {
                String uuid = namedDictionary.getDictionary()
                        .getUUID();

                enabledDictionaries.add(uuid);
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

    public void setDictionaryEnabled(@NonNull String uuid,
                                     boolean enabled) {

        boolean found = false;
        for (NamedDictionary namedDictionary : dictionaries) {
            String uuid1 = namedDictionary.getDictionary()
                    .getUUID();

            if (uuid.equals(uuid1)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Unknown dictionary UUID: " + uuid);
        }

        if (enabled) {
            enabledDictionaries.add(uuid);
        } else {
            enabledDictionaries.remove(uuid);
        }
    }

    public Observable<IDictionaryStatus> getDictionaries() {
        return Observable.create(
                new Observable.OnSubscribe<IDictionaryStatus>() {
                    @Override
                    public void call(Subscriber<? super IDictionaryStatus> subscriber) {
                        try {
                            for (NamedDictionary namedDictionary : dictionaries) {
                                String uuid = namedDictionary.getDictionary()
                                        .getUUID();

                                boolean enabled = enabledDictionaries.contains(uuid);
                                DictionaryStatus dictionaryStatus = new DictionaryStatus(namedDictionary, enabled);
                                subscriber.onNext(dictionaryStatus);
                            }

                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }
        );
    }

    static class DictionaryStatus implements IDictionaryStatus {

        private final NamedDictionary dictionary;
        private final boolean isEnabled;

        DictionaryStatus(NamedDictionary dictionary, boolean isEnabled) {
            this.dictionary = dictionary;
            this.isEnabled = isEnabled;
        }

        @Override
        public IDictionary getDictionary() {
            return dictionary.getDictionary();
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }

        @Override
        public String getName() {
            return dictionary.getName();
        }

    }

}
