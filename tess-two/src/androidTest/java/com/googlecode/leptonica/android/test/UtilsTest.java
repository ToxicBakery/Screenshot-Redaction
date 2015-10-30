package com.googlecode.leptonica.android.test;

import android.test.suitebuilder.annotation.SmallTest;

import com.googlecode.leptonica.android.test.TestUtils.Pixel;

import junit.framework.TestCase;

/**
 * @author ian.shaun.thomas@gmail.com (Ian Thomas)
 */
public class UtilsTest extends TestCase {

    private static final double DELTA = 1e-12d;

    @SmallTest
    public void testMetaPixelTest() {
        assertEquals(1d, TestUtils.comparePixels(0xffffffff, 0xffffffff), DELTA);
        assertEquals(0.75d, TestUtils.comparePixels(0xffffff00, 0xffffffff), DELTA);
        assertEquals(0.5d, TestUtils.comparePixels(0xffff0000, 0xffffffff), DELTA);
        assertEquals(0.25d, TestUtils.comparePixels(0xff000000, 0xffffffff), DELTA);

        assertEquals(0.75d, TestUtils.comparePixels(0xffffffff, 0xffffff00), DELTA);
        assertEquals(0.5d, TestUtils.comparePixels(0xffffffff, 0xffff0000), DELTA);
        assertEquals(0.25d, TestUtils.comparePixels(0xffffffff, 0xff000000), DELTA);
    }

    @SmallTest
    public void testPixel() {
        Pixel alpha = new Pixel(0xff000000);
        assertEquals(0xff, alpha.a);
        assertEquals(0x00, alpha.r);
        assertEquals(0x00, alpha.g);
        assertEquals(0x00, alpha.b);

        Pixel red = new Pixel(0x00ff0000);
        assertEquals(0x00, red.a);
        assertEquals(0xff, red.r);
        assertEquals(0x00, red.g);
        assertEquals(0x00, red.b);

        Pixel green = new Pixel(0x0000ff00);
        assertEquals(0x00, green.a);
        assertEquals(0x00, green.r);
        assertEquals(0xff, green.g);
        assertEquals(0x00, green.b);

        Pixel blue = new Pixel(0x000000ff);
        assertEquals(0x00, blue.a);
        assertEquals(0x00, blue.r);
        assertEquals(0x00, blue.g);
        assertEquals(0xff, blue.b);
    }

}
