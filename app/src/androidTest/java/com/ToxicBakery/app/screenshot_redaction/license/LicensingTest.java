package com.ToxicBakery.app.screenshot_redaction.license;

import android.test.AndroidTestCase;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LicensingTest extends AndroidTestCase {
    
    public void testGetInstance() throws Exception {
        Licensing.getInstance(getContext());
    }

    public void testGetLicenses() throws Exception {
        new Licensing(getContext())
                .getLicenses()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .flatMap(new Func1<License[], Observable<License>>() {
                    @Override
                    public Observable<License> call(License[] licenses) {
                        return Observable.from(licenses);
                    }
                })
                .map(new Func1<License, Observable<String>>() {
                    @Override
                    public Observable<String> call(final License license) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                try {
                                    String licenseText = license.getLicenseText(getContext());
                                    subscriber.onNext(licenseText);
                                    subscriber.onCompleted();
                                } catch (IOException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .subscribe(new Subscriber<Observable<String>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail(e.getMessage());
                    }

                    @Override
                    public void onNext(Observable<String> stringObservable) {
                        assertNotNull(stringObservable);
                    }
                });
    }
}