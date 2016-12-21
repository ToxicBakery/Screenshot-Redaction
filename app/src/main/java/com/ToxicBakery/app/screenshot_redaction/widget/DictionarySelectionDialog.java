package com.ToxicBakery.app.screenshot_redaction.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.dictionary.DictionaryProvider;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionary;
import com.ToxicBakery.app.screenshot_redaction.dictionary.IDictionaryStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DictionarySelectionDialog extends DialogFragment implements DialogInterface.OnMultiChoiceClickListener {

    public static final String TAG = "DictionarySelectionDialog";

    private DictionaryProvider dictionaryProvider;
    private CharSequence[] displayValues;
    private String[] entryValues;
    private boolean[] states;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dictionaryProvider = DictionaryProvider.getInstance(getActivity());

        dictionaryProvider.getDictionaries()
                .subscribe(new DictionaryAction());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_dictionaries)
                .setMultiChoiceItems(displayValues, states, this)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        String uuid = entryValues[which];
        dictionaryProvider.setDictionaryEnabled(uuid, isChecked)
                .subscribe();
    }

    class DictionaryAction extends Subscriber<IDictionaryStatus> {

        final List<CharSequence> displayValuesList = new LinkedList<>();
        final List<String> entryValuesList = new LinkedList<>();
        final List<Boolean> statesList = new ArrayList<>();

        @Override
        public void onCompleted() {
            displayValues = displayValuesList.toArray(new CharSequence[displayValuesList.size()]);
            entryValues = entryValuesList.toArray(new String[entryValuesList.size()]);
            states = new boolean[statesList.size()];

            for (int i = 0; i < statesList.size(); i++) {
                states[i] = statesList.get(i);
            }
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(IDictionaryStatus dictionaryStatus) {
            IDictionary dictionary = dictionaryStatus.getDictionary();
            String displayName = dictionaryStatus.getName();
            String entryValue = dictionary.getUUID();
            boolean enabled = dictionaryStatus.isEnabled();

            displayValuesList.add(displayName);
            entryValuesList.add(entryValue);
            statesList.add(enabled);
        }

    }

}
