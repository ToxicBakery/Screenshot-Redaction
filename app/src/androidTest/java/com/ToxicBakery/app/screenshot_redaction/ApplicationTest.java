package com.ToxicBakery.app.screenshot_redaction;

import android.app.Application;
import android.test.ApplicationTestCase;

import jonathanfinerty.once.Once;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Once.initialise(getContext());
    }

}