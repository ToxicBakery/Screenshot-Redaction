package com.ToxicBakery.app.screenshot_redaction.fragment;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ToxicBakery.app.screenshot_redaction.R;

import de.greenrobot.event.EventBus;

public class FragmentTutorial extends Fragment implements View.OnClickListener {

    public static final int ARROW_RIGHT = 1;

    private static final String EXTRA_TEXT = "EXTRA_TEXT";
    private static final String EXTRA_IMAGE = "EXTRA_IMAGE";

    private static final EventBus ARROW_SELECT_BUS = EventBus.builder()
            .logNoSubscriberMessages(true)
            .logSubscriberExceptions(true)
            .throwSubscriberException(true)
            .build();

    public static EventBus getEventBus() {
        return ARROW_SELECT_BUS;
    }

    public static FragmentTutorial createInstance(@StringRes int description,
                                                  @DrawableRes int image) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TEXT, description);
        bundle.putInt(EXTRA_IMAGE, image);

        FragmentTutorial fragmentTutorial = new FragmentTutorial();
        fragmentTutorial.setArguments(bundle);
        return fragmentTutorial;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int description = bundle.getInt(EXTRA_TEXT);
        int image = bundle.getInt(EXTRA_IMAGE);

        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        ((TextView) view.findViewById(R.id.tutorial_description)).setText(description);
        ((ImageView) view.findViewById(R.id.tutorial_image)).setImageResource(image);

        view.findViewById(R.id.tutorial_arrow_right)
                .setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tutorial_arrow_right:
                moveToNextPage();
                break;
        }
    }

    void moveToNextPage() {
        ARROW_SELECT_BUS.post(ARROW_RIGHT);
    }

}
