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

public class DictionaryEnglishTest extends AndroidTestCase {
    
    private final Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        CopyToSdCard.getEventBus()
                .register(this, Integer.MIN_VALUE);

        DictionaryEnglish.getInstance(getContext());
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
        DictionaryEnglish.getInstance(getContext());
    }

    public void testGetName() throws Exception {
        DictionaryEnglish dictionary = new DictionaryEnglish(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals(R.string.dict_en, dictionary.getName());
    }

    public void testGetUUID() throws Exception {
        DictionaryEnglish dictionary = new DictionaryEnglish(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals("9c38766b-266f-4841-84e0-b28171982a63", dictionary.getUUID());
    }
    
    public void testShouldRedact() throws Exception {
        DictionaryEnglish dictionary = new DictionaryEnglish(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        DB db = DBMaker.fileDB(dictionary.copyConfiguration.getTarget())
                .closeOnJvmShutdown()
                .readOnly()
                .make();

        Set<String> words = db.hashSet("wordlist");
        for (String word : words) {
            assertFalse("Invalid redaction of " + word, dictionary.shouldRedact(word));
        }

        // Test special characters
        assertFalse(dictionary.shouldRedact("hello"));
        assertFalse(dictionary.shouldRedact("world"));
        assertFalse(dictionary.shouldRedact("#hello"));
        assertFalse(dictionary.shouldRedact("#world"));
        assertFalse(dictionary.shouldRedact("@hello"));
        assertFalse(dictionary.shouldRedact("@world"));
        assertFalse(dictionary.shouldRedact("hello."));
        assertFalse(dictionary.shouldRedact("world."));
        assertFalse(dictionary.shouldRedact("hello!"));
        assertFalse(dictionary.shouldRedact("world!"));
        assertFalse(dictionary.shouldRedact("hello?"));
        assertFalse(dictionary.shouldRedact("world?"));
        
        assertTrue(dictionary.shouldRedact("test@email.com"));
        assertTrue(dictionary.shouldRedact("google.com"));
        assertTrue(dictionary.shouldRedact("http://google.com"));
    }
    
}