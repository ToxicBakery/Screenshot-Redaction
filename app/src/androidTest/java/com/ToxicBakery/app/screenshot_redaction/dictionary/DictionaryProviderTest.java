package com.ToxicBakery.app.screenshot_redaction.dictionary;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ToxicBakery.app.screenshot_redaction.ActivityTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DictionaryProviderTest {

    @Rule
    public ActivityTestRule<ActivityTest> activityTestRule = new ActivityTestRule<>(ActivityTest.class);

    private Context getContext() {
        return activityTestRule.getActivity();
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

    @Test
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