package com.ToxicBakery.app.screenshot_redaction.bus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Default implementation of the bus.
 *
 * @param <T> to publish and subscribe
 */
public abstract class ADefaultBus<T> implements IBus<T> {

    private final Subject<T, T> subject = new SerializedSubject<>(PublishSubject.<T>create());

    @Override
    public Observable<T> register() {
        return subject;
    }

    @Override
    public void post(T t) {
        subject.onNext(t);
    }

}
