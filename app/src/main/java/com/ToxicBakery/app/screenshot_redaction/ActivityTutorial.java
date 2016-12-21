package com.ToxicBakery.app.screenshot_redaction;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ToxicBakery.app.screenshot_redaction.bus.TutorialBus;
import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentTutorial;
import com.ToxicBakery.app.screenshot_redaction.recycler.TutorialAdapter;
import com.ToxicBakery.app.screenshot_redaction.service.ScreenshotService;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ActivityTutorial extends AppCompatActivity {

    private TutorialBus tutorialBus;
    private Subscription tutorialSubscription;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tutorialBus = TutorialBus.getInstance();

        PagerAdapter adapter = new TutorialAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScreenshotService.startScreenshotService(this);

        tutorialSubscription = tutorialBus.register()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer arrowDirection) {
                        switch (arrowDirection) {
                            case FragmentTutorial.ARROW_RIGHT:
                                int position = viewPager.getCurrentItem();
                                viewPager.setCurrentItem(position + 1);
                                break;
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();

        tutorialSubscription.unsubscribe();
    }

}
