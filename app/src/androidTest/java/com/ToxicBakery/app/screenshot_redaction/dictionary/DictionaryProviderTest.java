package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.test.AndroidTestCase;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class DictionaryProviderTest extends AndroidTestCase {

    public void testGetInstance() throws Exception {
        DictionaryProvider.getInstance(getContext());
    }

    public void testSetDictionaryEnabled() throws Exception {
        final DictionaryProvider dictionaryProvider = DictionaryProvider.getInstance(getContext());
        dictionaryProvider.getDictionaries()
                .flatMap(new Func1<IDictionaryStatus, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(IDictionaryStatus dictionaryStatus) {
                        String uuid = dictionaryStatus.getDictionary().getUUID();
                        return dictionaryProvider.setDictionaryEnabled(uuid, false);
                    }
                })
                .subscribe();

        dictionaryProvider.getDictionaries()
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                        assertFalse(dictionaryStatus.isEnabled());
                    }
                });

        dictionaryProvider.getDictionaries()
                .flatMap(new Func1<IDictionaryStatus, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(IDictionaryStatus dictionaryStatus) {
                        String uuid = dictionaryStatus.getDictionary().getUUID();
                        return dictionaryProvider.setDictionaryEnabled(uuid, true);
                    }
                });

        dictionaryProvider.getDictionaries()
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                        assertTrue(dictionaryStatus.isEnabled());
                    }
                });
    }

    public void testGetDictionaries() throws Exception {
        DictionaryProvider dictionaryProvider = DictionaryProvider.getInstance(getContext());
        dictionaryProvider.getDictionaries()
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                    }
                });
    }

}