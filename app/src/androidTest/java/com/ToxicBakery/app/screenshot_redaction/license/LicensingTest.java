package com.ToxicBakery.app.screenshot_redaction.license;

import android.test.AndroidTestCase;

public class LicensingTest extends AndroidTestCase {

    public void testGetLicenses() throws Exception {
        License[] licenses = Licensing.getLicenses(getContext())
                .toBlocking()
                .first();

        assertNotNull(licenses);
        assertEquals(18, licenses.length);
    }

}