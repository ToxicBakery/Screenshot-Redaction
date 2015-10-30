package com.ToxicBakery.app.screenshot_redaction.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ToxicBakery.android.version.Is;
import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.TessDataRawResourceCopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglish;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglishNames;
import com.ToxicBakery.app.screenshot_redaction.service.ScreenshotService;
import com.ToxicBakery.app.screenshot_redaction.util.PermissionCheck;

import rx.Observable;
import rx.functions.Action1;

import static com.ToxicBakery.android.version.SdkVersion.MARSHMALLOW;

public class FragmentInitialize extends Fragment implements View.OnClickListener {

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Scene sceneComplete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_initialize, container, false);

        if (Is.greaterThanOrEqual(MARSHMALLOW)) {
            view.findViewById(R.id.tutorial_accept)
                    .setOnClickListener(this);

            ViewGroup sceneRoot = (ViewGroup) view.findViewById(R.id.scene_root);
            sceneComplete = Scene.getSceneForLayout(sceneRoot, R.layout.scene_finished, getContext());

            if (hasPermissions()) {
                sceneComplete.enter();
            }
        } else {
            view.findViewById(R.id.tutorial_done)
                    .setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (Is.lessThan(MARSHMALLOW) || hasPermissions()) {
                performCopy();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tutorial_accept:
                if (hasPermissions()) {
                    performCopy();
                    animateFinished();
                } else {
                    requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.tutorial_done:
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Observable.just(hasPermissions())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean hasPermission) {
                        if (hasPermission) {
                            performCopy();
                            animateFinished();
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void animateFinished() {
        Transition slideFromEnd = new Slide(Gravity.END);
        TransitionManager.go(sceneComplete, slideFromEnd);

        sceneComplete.getSceneRoot()
                .findViewById(R.id.tutorial_done)
                .setOnClickListener(this);
    }

    @MainThread
    void performCopy() {
        Context context = getContext();
        CopyToSdCard.copy(new TessDataRawResourceCopyConfiguration(context, R.raw.eng));
        DictionaryEnglish.getInstance(context);
        DictionaryEnglishNames.getInstance(context);
        ScreenshotService.startScreenshotService(context);
    }

    @MainThread
    boolean hasPermissions() {
        return Is.lessThan(MARSHMALLOW)
                || PermissionCheck.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
    }

}
