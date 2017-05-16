package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.functions.Func1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DictionaryProviderTest {

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testGetInstance() throws Exception {
        DictionaryProvider.getInstance(getContext());
    }

    @Test
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
                .toBlocking()
                .first();

        assertFalse(dictionaryProvider.getDictionaries()
                .toBlocking()
                .first()
                .isEnabled());

        dictionaryProvider.getDictionaries()
                .flatMap(new Func1<IDictionaryStatus, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(IDictionaryStatus dictionaryStatus) {
                        String uuid = dictionaryStatus.getDictionary().getUUID();
                        return dictionaryProvider.setDictionaryEnabled(uuid, true);
                    }
                })
                .toBlocking()
                .first();

        assertTrue(dictionaryProvider.getDictionaries()
                .toBlocking()
                .first()
                .isEnabled());
    }

    @Test
    public void testGetDictionaries() throws Exception {
        DictionaryProvider dictionaryProvider = DictionaryProvider.getInstance(getContext());
        assertNotNull(dictionaryProvider.getDictionaries()
                .toBlocking()
                .first());
    }

}