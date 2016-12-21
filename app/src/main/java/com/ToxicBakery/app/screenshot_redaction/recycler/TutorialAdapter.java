package com.ToxicBakery.app.screenshot_redaction.recycler;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentInitialize;
import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentTutorial;

public class TutorialAdapter extends FragmentPagerAdapter {

    private static final Fragment[] fragments = {
            FragmentTutorial.createInstance(R.string.tutorial_screenshot, R.drawable.tutorial_frame_screenshot),
            FragmentTutorial.createInstance(R.string.tutorial_redact, R.drawable.tutorial_frame_redact),
            FragmentTutorial.createInstance(R.string.tutorial_share, R.drawable.tutorial_frame_share),
            new FragmentInitialize()
    };

    public TutorialAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
