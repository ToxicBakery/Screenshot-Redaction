package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bluelinelabs.logansquare.LoganSquare;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LicenseTest {

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testWriteToParcel() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");

        Parcel parcel = Parcel.obtain();
        license.writeToParcel(parcel, 0);
        byte[] marshall = parcel.marshall();
        parcel.recycle();

        parcel = Parcel.obtain();
        parcel.unmarshall(marshall, 0, marshall.length);
        parcel.setDataPosition(0);
        License clone = new License(parcel);

        assertEquals(license.getName(), clone.getName());
        assertEquals(license.getLicenseIdentifier(), clone.getLicenseIdentifier());
    }

    @Test
    public void testDescribeContents() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals(0, license.describeContents());
    }

    @Test
    public void testGetLicenseIdentifier() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals("license_tesseract", license.getLicenseIdentifier());
    }

    @Test
    public void testGetName() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals("Tesseract", license.getName());
    }

    @Test
    public void testGetLicenseText() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        Resources resources = getContext().getResources();
        int identifier = resources.getIdentifier(license.getLicenseIdentifier(), "raw", getContext().getPackageName());

        String readString = readFully(resources.openRawResource(identifier));
        String licenseText = license.getLicenseText(getContext());

        assertEquals(readString, licenseText);
    }

    private String readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

    private License createLicense(String name, String licenseIdentifier) throws IOException {
        String license = String.format(
                Locale.ENGLISH,
                "{\"name\":%s,\"licenseIdentifier\":%s}",
                name,
                licenseIdentifier
        );
        return LoganSquare.parse(license, License.class);
    }

}