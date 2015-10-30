package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.res.Resources;
import android.os.Parcel;
import android.test.AndroidTestCase;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class LicenseTest extends AndroidTestCase {
    
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

    public void testDescribeContents() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals(0, license.describeContents());
    }

    public void testGetLicenseIdentifier() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals("license_tesseract", license.getLicenseIdentifier());
    }

    public void testGetName() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        assertEquals("Tesseract", license.getName());
    }

    public void testGetLicenseText() throws Exception {
        License license = createLicense("Tesseract", "license_tesseract");
        Resources resources = getContext().getResources();
        int identifier = resources.getIdentifier(license.getLicenseIdentifier(), "raw", getContext().getPackageName());

        String readString = readFully(resources.openRawResource(identifier));
        String licenseText = license.getLicenseText(getContext());

        assertEquals(readString, licenseText);
    }

    String readFully(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            return new String(byteArrayOutputStream.toByteArray());
        } finally {
            inputStream.close();
        }
    }

    License createLicense(String name, String licenseIdentifier) {
        String license = String.format(
                Locale.ENGLISH,
                "{\"name\":%s,\"licenseIdentifier\":%s}",
                name,
                licenseIdentifier
        );
        return new Gson().fromJson(license, License.class);
    }

}