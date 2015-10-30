package com.ToxicBakery.app.screenshot_redaction.dictionary.impl;

import android.test.AndroidTestCase;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.WordListRawResourceCopyConfiguration;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DictionaryEnglishNamesTest extends AndroidTestCase {

    private final Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        CopyToSdCard.getEventBus()
                .register(this, Integer.MIN_VALUE);

        DictionaryEnglishNames.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        CopyToSdCard.getEventBus()
                .unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(WordListRawResourceCopyConfiguration copyConfiguration) {
        semaphore.release();
    }

    public void testGetInstance() throws Exception {
        DictionaryEnglishNames.getInstance(getContext());
    }

    public void testShouldRedact() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        DB db = DBMaker.fileDB(dictionary.copyConfiguration.getTarget())
                .closeOnJvmShutdown()
                .readOnly()
                .make();

        Set<String> words = db.hashSet("wordlist");
        int count = 0;
        for (String word : words) {
            assertTrue("Invalid redaction of " + word + " " + count, dictionary.shouldRedact(word));
            ++count;
        }
    }

    public void testGetName() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals(R.string.dict_en_names, dictionary.getName());
    }

    public void testGetUUID() throws Exception {
        DictionaryEnglishNames dictionary = new DictionaryEnglishNames(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals("be401e34-e046-46cf-8958-59efedb7d260", dictionary.getUUID());
    }

}