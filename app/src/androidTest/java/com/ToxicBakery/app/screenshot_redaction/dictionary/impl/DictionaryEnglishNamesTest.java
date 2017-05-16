package com.ToxicBakery.app.screenshot_redaction.dictionary.impl;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.bus.CopyBus;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.WordListRawResourceCopyConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DictionaryEnglishNamesTest {

    private final Semaphore semaphore = new Semaphore(0);
    private Subscription subscription;

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        subscription = CopyBus.getInstance()
                .register()
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<CopyToSdCard.ICopyConfiguration>() {
                    @Override
                    public void call(CopyToSdCard.ICopyConfiguration iCopyConfiguration) {
                        semaphore.release();
                    }
                });

        DictionaryEnglishNames.getInstance(getContext());
    }

    @After
    public void tearDown() throws Exception {
        subscription.unsubscribe();
    }

    @SuppressWarnings("unused")
    public void onEvent(WordListRawResourceCopyConfiguration copyConfiguration) {
        semaphore.release();
    }

    @Test
    public void testGetInstance() throws Exception {
        DictionaryEnglishNames.getInstance(getContext());
    }

    @Test
    public void testShouldRedact() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        DB db = DBMaker.fileDB(dictionary.copyConfiguration.getTarget())
                .closeOnJvmShutdown()
                .readOnly()
                .make();

        Set<String> words = db.hashSet(dictionary.getUUID());
        int count = 0;
        for (String word : words) {
            assertTrue("Invalid redaction of " + word + " " + count, dictionary.shouldRedact(word));
            ++count;
        }
    }

    @Test
    public void testGetName() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals(R.string.dict_en_names, dictionary.getName());
    }

    @Test
    public void testGetUUID() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals("be401e34-e046-46cf-8958-59efedb7d260", dictionary.getUUID());
    }

}