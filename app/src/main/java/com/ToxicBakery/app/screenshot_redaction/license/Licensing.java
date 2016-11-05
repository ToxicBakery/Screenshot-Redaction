package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

public final class Licensing {

    private static volatile License[] licenses;

    private Licensing() {
    }

    public static Observable<License[]> getLicenses(@NonNull Context context) {
        return Observable.just(context.getApplicationContext())
                .map(new Func1<Context, License[]>() {
                    @Override
                    public License[] call(Context context) {
                        synchronized (Licensing.class) {
                            if (licenses == null) {
                                InputStream inputStream = context.getResources().openRawResource(R.raw.licensing_list);
                                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                                try {
                                    licenses = new Gson().fromJson(inputStreamReader, License[].class);
                                } catch (Exception e) {
                                    throw Exceptions.propagate(e);
                                }
                            }
                        }

                        return licenses;
                    }
                });
    }

}
