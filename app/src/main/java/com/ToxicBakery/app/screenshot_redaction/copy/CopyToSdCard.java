package com.ToxicBakery.app.screenshot_redaction.copy;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.schedulers.Schedulers;

public class CopyToSdCard {

    public void copy(@NonNull ICopyConfiguration copy) {
        Observable.just(copy)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new CopyAction());
    }

    public interface ICopyConfiguration {

        InputStream getCopyStream() throws IOException;

        File getTarget() throws IOException;

        long getSize() throws IOException;

    }

}
