package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ToxicBakery.app.screenshot_redaction.ActivityTest;

import org.junit.Rule;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LicensingTest {

    @Rule
    public ActivityTestRule<ActivityTest> activityTestRule = new ActivityTestRule<>(ActivityTest.class);

    private Context getContext() {
        return activityTestRule.getActivity();
    }

    public void testGetLicenses() throws Exception {
        License[] licenses = Licensing.getLicenses(getContext())
                .toBlocking()
                .first();

        assertNotNull(licenses);
        assertEquals(18, licenses.length);
    }

}