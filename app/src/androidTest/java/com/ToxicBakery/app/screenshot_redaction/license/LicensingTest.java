package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LicensingTest {

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    public void testGetLicenses() throws Exception {
        License[] licenses = Licensing.getLicenses(getContext())
                .toBlocking()
                .first();

        assertNotNull(licenses);
        assertEquals(18, licenses.length);
    }

}