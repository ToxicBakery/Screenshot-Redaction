package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.test.AndroidTestCase;

import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DictionaryProviderTest extends AndroidTestCase {
    
    public void testGetInstance() throws Exception {
        DictionaryProvider.getInstance(getContext());
    }

    public void testSetDictionaryEnabled() throws Exception {
        final DictionaryProvider dictionaryProvider = DictionaryProvider.getInstance(getContext());
        dictionaryProvider.getDictionaries()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                        dictionaryProvider.setDictionaryEnabled(dictionaryStatus.getDictionary(), false);
                    }
                });

        dictionaryProvider.getDictionaries()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                        assertFalse(dictionaryStatus.isEnabled());
                    }
                });

        dictionaryProvider.getDictionaries()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .subscribe(new Action1<IDictionaryStatus>() {
                    @Override
                    public void call(IDictionaryStatus dictionaryStatus) {
                        dictionaryProvider.setDictionaryEnabled(dictionaryStatus.getDictionary(), true);
                    }
                });

        dictionaryProvider.getDictionaries()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
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
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .subscribe(new Subscriber<IDictionaryStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail(e.getMessage());
                    }

                    @Override
                    public void onNext(IDictionaryStatus dictionaryStatus) {
                        assertTrue(dictionaryStatus.isEnabled());
                    }
                });
    }

}