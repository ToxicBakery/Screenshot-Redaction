package com.ToxicBakery.app.screenshot_redaction.bus;

import rx.Observable;

/**
 * Event bus for registering and publishing for specific events.
 *
 * @param <T> to publish and subscribe
 */
@SuppressWarnings("WeakerAccess")
public interface IBus<T> {

    /**
     * An observable that returns events posted to the bus.
     *
     * @return stream of posted events
     */
    Observable<T> register();

    /**
     * Post an event to the stream.
     *
     * @param t to post
     */
    void post(T t);

}
