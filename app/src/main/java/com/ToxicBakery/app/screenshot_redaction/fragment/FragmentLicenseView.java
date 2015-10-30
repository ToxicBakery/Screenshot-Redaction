package com.ToxicBakery.app.screenshot_redaction.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.license.License;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class FragmentLicenseView extends Fragment {

    public static final String TAG = "FragmentLicenseView";

    private static final String EXTRA_LICENSE = "EXTRA_LICENSE";

    private License license;
    private TextView textViewLicense;
    private Subscription subscribeLoadLicense;

    public static Fragment createInstance(License license) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_LICENSE, license);

        FragmentLicenseView fragmentLicenseView = new FragmentLicenseView();
        fragmentLicenseView.setArguments(bundle);

        return fragmentLicenseView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            license = bundle.getParcelable(EXTRA_LICENSE);
        }

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_license, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewLicense = (TextView) view.findViewById(R.id.license);
    }

    @Override
    public void onResume() {
        super.onResume();

        subscribeLoadLicense = getLicenseText().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textViewLicense.setText(s);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (subscribeLoadLicense != null && !subscribeLoadLicense.isUnsubscribed()) {
            subscribeLoadLicense.unsubscribe();
        }
    }

    Observable<String> getLicenseText() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                return Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            String licenseText = license.getLicenseText(getContext());
                            subscriber.onNext(licenseText);
                            subscriber.onCompleted();
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }
                });
            }
        });
    }

}
