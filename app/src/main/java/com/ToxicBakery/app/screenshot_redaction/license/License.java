package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
public final class License implements Parcelable {

    public static final Creator<License> CREATOR = new Creator<License>() {
        @Override
        public License createFromParcel(Parcel in) {
            return new License(in);
        }

        @Override
        public License[] newArray(int size) {
            return new License[size];
        }
    };

    private String name;
    private String licenseIdentifier;

    protected License(Parcel in) {
        name = in.readString();
        licenseIdentifier = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(licenseIdentifier);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLicenseIdentifier() {
        return licenseIdentifier;
    }

    public String getName() {
        return name;
    }

    @WorkerThread
    public String getLicenseText(@NonNull Context context) throws IOException {
        context = context.getApplicationContext();
        Resources resources = context.getResources();

        final int identifier = resources.getIdentifier(
                licenseIdentifier,
                "raw",
                context.getPackageName()
        );

        try (InputStream inputStream = resources.openRawResource(identifier)) {
            return IOUtils.toString(inputStream);
        }
    }

}
