package com.ToxicBakery.app.screenshot_redaction.license;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Observable;
import rx.Subscriber;

public class Licensing {

    static Licensing instance;

    final Context context;

    License[] licenses;

    Licensing(@NonNull Context context) {
        this.context = context;
    }

    public static Licensing getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (Licensing.class) {
                if (instance == null) {
                    instance = new Licensing(context);
                }
            }
        }

        return instance;
    }

    public Observable<License[]> getLicenses() {
        return Observable.create(new Observable.OnSubscribe<License[]>() {
            @Override
            public void call(Subscriber<? super License[]> subscriber) {

                synchronized (Licensing.this) {
                    if (licenses == null) {
                        InputStream inputStream = context.getResources()
                                .openRawResource(R.raw.licensing_list);
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        try {
                            licenses = new Gson()
                                    .fromJson(inputStreamReader, License[].class);
                        } catch (Exception e) {
                            subscriber.onError(e);
                            return;
                        }
                    }
                }

                subscriber.onNext(licenses);
                subscriber.onCompleted();
            }
        });
    }

}
