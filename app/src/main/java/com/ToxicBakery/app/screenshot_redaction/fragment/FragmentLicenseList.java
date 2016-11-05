package com.ToxicBakery.app.screenshot_redaction.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.license.License;
import com.ToxicBakery.app.screenshot_redaction.license.Licensing;
import com.ToxicBakery.app.screenshot_redaction.widget.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class FragmentLicenseList extends Fragment {

    public static final String TAG = "FragmentLicenseList";

    private static RxBus<License> licenseRxBus = new RxBus<>();

    private Adapter adapter;
    private Subscription subscribeLoadLicenses;
    private Subscription subscriptionRxBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_license_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new Adapter();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        subscribeLoadLicenses = Licensing.getLicenses(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<License[]>() {
                    @Override
                    public void call(License[] licenses) {
                        adapter.setLicenses(licenses);
                        adapter.notifyDataSetChanged();
                    }
                });

        subscriptionRxBus = licenseRxBus.toObserverable()
                .subscribe(new Action1<License>() {
                    @Override
                    public void call(License license) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, FragmentLicenseView.createInstance(license), FragmentLicenseView.TAG)
                                .addToBackStack(null)
                                .commit();
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (subscribeLoadLicenses != null && !subscribeLoadLicenses.isUnsubscribed()) {
            subscribeLoadLicenses.unsubscribe();
        }

        if (subscriptionRxBus != null && !subscriptionRxBus.isUnsubscribed()) {
            subscriptionRxBus.unsubscribe();
        }
    }

    /**
     * @param <T>
     * @see <a href="http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/">http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/</a>
     */
    static public class RxBus<T> {

        private final PublishSubject<T> publishSubject = PublishSubject.create();
        private final Subject<T, T> bus = new SerializedSubject<>(publishSubject);

        public void send(T o) {
            bus.onNext(o);
        }

        public Observable<T> toObserverable() {
            return bus;
        }

    }

    static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<License> licenseList;

        public Adapter() {
            licenseList = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selectable_simple_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            License license = licenseList.get(position);
            holder.bind(license);
        }

        @Override
        public int getItemCount() {
            return licenseList.size();
        }

        void setLicenses(License[] licenses) {
            licenseList.clear();
            licenseList.addAll(Arrays.asList(licenses));
            Collections.sort(licenseList, new Comparator<License>() {
                @Override
                public int compare(License lhs, License rhs) {
                    return lhs.getName()
                            .compareToIgnoreCase(rhs.getName());
                }
            });
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView textViewLicense;

        private License license;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewLicense = (TextView) itemView.findViewById(android.R.id.text1);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            licenseRxBus.send(license);
        }

        void bind(License license) {
            textViewLicense.setText(license.getName());
            this.license = license;
        }

    }

}
