package com.ToxicBakery.app.screenshot_redaction.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ToxicBakery.app.screenshot_redaction.ActivityLicenses;
import com.ToxicBakery.app.screenshot_redaction.R;
import com.ToxicBakery.app.screenshot_redaction.widget.DictionarySelectionDialog;

public class FragmentSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final String TAG = "FragmentSettings";
    public static final String PREF_LICENSES = "pref_licenses";
    public static final String PREF_DICTIONARIES = "pref_dictionaries";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        findPreference(PREF_LICENSES).setOnPreferenceClickListener(this);
        findPreference(PREF_DICTIONARIES).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PREF_LICENSES:
                Intent intent = new Intent(getActivity(), ActivityLicenses.class);
                startActivity(intent);
                return true;
            case PREF_DICTIONARIES:
                new DictionarySelectionDialog().show(getFragmentManager(), DictionarySelectionDialog.TAG);
                return true;
        }
        return false;
    }

}
