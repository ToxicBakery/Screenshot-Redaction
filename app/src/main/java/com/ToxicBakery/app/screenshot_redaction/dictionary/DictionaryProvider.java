package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglish;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglishNames;
import com.pacoworks.rxtuples.RxTuples;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DictionaryProvider {

    private static final String DB_NAME = "DictionaryProvider.mapdb";
    private static final Scheduler SCHEDULER = Schedulers.from(
            new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));

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

        Observable.just(Pair.with(exists, dictionaries))
                .filter(new Func1<Pair<Boolean, NamedDictionary[]>, Boolean>() {
                    @Override
                    public Boolean call(Pair<Boolean, NamedDictionary[]> pair) {
                        return !pair.getValue0();
                    }
                })
                .flatMap(new Func1<Pair<Boolean, NamedDictionary[]>, Observable<NamedDictionary>>() {
                    @Override
                    public Observable<NamedDictionary> call(Pair<Boolean, NamedDictionary[]> pair) {
                        return Observable.from(pair.getValue1());
                    }
                })
                .subscribe(new Action1<NamedDictionary>() {
                    @Override
                    public void call(NamedDictionary namedDictionary) {
                        enabledDictionaries.add(namedDictionary.getDictionary().getUUID());
                    }
                });
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
        return Observable.zip(
                Observable.just(uuid).repeat(),
                Observable.just(enabled).repeat(),
                Observable.from(dictionaries),
                RxTuples.<String, Boolean, NamedDictionary>toTriplet())
                .filter(new Func1<Triplet<String, Boolean, NamedDictionary>, Boolean>() {
                    @Override
                    public Boolean call(Triplet<String, Boolean, NamedDictionary> triplet) {
                        String uuid = triplet.getValue0();
                        return enabledDictionaries.contains(uuid);
                    }
                })
                .map(new Func1<Triplet<String, Boolean, NamedDictionary>, Boolean>() {
                    @Override
                    public Boolean call(Triplet<String, Boolean, NamedDictionary> triplet) {
                        String uuid = triplet.getValue0();
                        Boolean enabled = triplet.getValue1();
                        return enabled ? enabledDictionaries.add(uuid) : enabledDictionaries.remove(uuid);
                    }
                })
                .subscribeOn(SCHEDULER);
    }

    public Observable<IDictionaryStatus> getDictionaries() {
        return Observable.from(dictionaries)
                .map(new Func1<NamedDictionary, IDictionaryStatus>() {
                    @Override
                    public IDictionaryStatus call(NamedDictionary namedDictionary) {
                        String uuid = namedDictionary.getDictionary()
                                .getUUID();

                        boolean enabled = enabledDictionaries.contains(uuid);
                        return new DictionaryStatus(namedDictionary, enabled);
                    }
                })
                .subscribeOn(SCHEDULER)
                .asObservable();
    }

}
