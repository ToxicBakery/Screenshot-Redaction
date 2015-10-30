package com.ToxicBakery.app.screenshot_redaction;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentInitialize;
import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentTutorial;
import com.ToxicBakery.app.screenshot_redaction.service.ScreenshotService;

public class ActivityTutorial extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PagerAdapter adapter = new TutorialAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScreenshotService.startScreenshotService(this);

        FragmentTutorial.getEventBus()
                .register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FragmentTutorial.getEventBus()
                .unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Integer arrowDirection) {
        switch (arrowDirection) {
            case FragmentTutorial.ARROW_RIGHT:
                int position = viewPager.getCurrentItem();
                viewPager.setCurrentItem(position + 1);
                break;
        }
    }


    static class TutorialAdapter extends FragmentPagerAdapter {

        static final Fragment[] fragments = {
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

}
