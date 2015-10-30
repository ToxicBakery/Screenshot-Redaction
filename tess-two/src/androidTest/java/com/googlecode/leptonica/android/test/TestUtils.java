/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.leptonica.android.test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;

/**
 * Utility methods for running Leptonica unit tests.
 *
 * @author alanv@google.com (Alan Viverette)
 */
public class TestUtils {
    public static float compareBitmaps(Bitmap a, Bitmap b) {
        int found = 0;

        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getPixel(x, y) == b.getPixel(x, y)) {
                    found++;
                }
            }
        }

        return found / (float)(a.getWidth() * a.getHeight());
    }

    public static float compareImages(Pix a, Bitmap b) {
        int found = 0;
        int miss = 0;

        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getPixel(x, y) == b.getPixel(x, y)) {
                    ++found;
                } else {
                    ++miss;
                    Log.d("TestUtils", x + " " + y);
                }
            }
        }

//        Assert.fail(found + " " + miss + " " + a.getWidth() + " " + a.getHeight());
        return found / (float)(a.getWidth() * a.getHeight());
    }

    public static float comparePix(Pix a, Pix b) {
        double found = 0;

        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                int pA = a.getPixel(x, y);
                int pB = b.getPixel(x, y);
                if (pA == pB) {
                    found++;
                } else {
                    // The pixels near hard edges seem to be frequently off so try and measure the difference
                    found += comparePixels(pA, pB);
                }
            }
        }

        return (float) (found / (double) (a.getWidth() * a.getHeight()));
    }

    public static Bitmap createTestBitmap(int width, int height, Bitmap.Config format) {
        Bitmap bmp = Bitmap.createBitmap(width, height, format);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        if (width >= 1 && height >= 1) {
            // Paint the left half white
            paint.setColor(Color.WHITE);
            paint.setStyle(Style.FILL);
            canvas.drawRect(new Rect(0, 0, width / 2, height), paint);

            if (width > 1) {
                // Paint the right half black
                paint.setColor(Color.BLACK);
                paint.setStyle(Style.FILL);
                canvas.drawRect(new Rect(width / 2, 0, width, height), paint);
            }
        }
        return bmp;
    }

    /**
     * Compare pixels returning a value of 0 - 1. A value of 1 is an exact pixel match while a
     * value of 0 would be a pixel color of black compared to white.
     *
     * @param a pixel
     * @param b pixel
     * @return a value between 0 and 1
     */
    static double comparePixels(int a, int b) {
        Pixel pA = new Pixel(a);
        Pixel pB = new Pixel(b);

        double diff = 0d;
        diff += Math.abs(pA.a - pB.a) / 255d;
        diff += Math.abs(pA.r - pB.r) / 255d;
        diff += Math.abs(pA.g - pB.g) / 255d;
        diff += Math.abs(pA.b - pB.b) / 255d;
        diff /= 4d;

        return 1 - diff;
    }

    /**
     * A representation of an ARGB pixel value.
     */
    static class Pixel {
        final int a, r, g, b;

        Pixel(int p) {
            a = (p >>> 24) & 0xff;
            r = (p >>> 16) & 0xff;
            g = (p >>> 8) & 0xff;
            b = p & 0xff;
        }

    }

    public static Pix createTestPix(int width, int height) {
        Bitmap bmp = TestUtils.createTestBitmap(width, height, Bitmap.Config.ARGB_8888);
        return ReadFile.readBitmap(bmp);
    }
}