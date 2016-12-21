package com.ToxicBakery.app.screenshot_redaction.dictionary.impl;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.ActivityTest;
import com.ToxicBakery.app.screenshot_redaction.bus.CopyBus;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.WordListRawResourceCopyConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DictionaryEnglishTest {

    private final Semaphore semaphore = new Semaphore(0);
    @Rule
    public ActivityTestRule<ActivityTest> activityTestRule = new ActivityTestRule<>(ActivityTest.class);
    private Subscription subscription;

    private Context getContext() {
        return activityTestRule.getActivity();
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

        DictionaryEnglish.getInstance(getContext());
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
        DictionaryEnglish.getInstance(getContext());
    }

    @Test
    public void testGetName() throws Exception {
        DictionaryEnglish dictionary = new DictionaryEnglish(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals(R.string.dict_en, dictionary.getName());
    }

    @Test
    public void testGetUUID() throws Exception {
        DictionaryEnglish dictionary = new DictionaryEnglish(getContext());
        assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS));
        assertEquals("9c38766b-266f-4841-84e0-b28171982a63", dictionary.getUUID());
    }

    @Test
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