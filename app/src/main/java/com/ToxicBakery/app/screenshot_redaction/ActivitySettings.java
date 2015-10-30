package com.ToxicBakery.app.screenshot_redaction;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ToxicBakery.android.version.Is;
import com.ToxicBakery.app.screenshot_redaction.fragment.FragmentSettings;
import com.ToxicBakery.app.screenshot_redaction.util.PermissionCheck;

import jonathanfinerty.once.Once;

import static com.ToxicBakery.android.version.SdkVersion.MARSHMALLOW;

public class ActivitySettings extends AppCompatActivity {

    private static final String KEY_SHOW_TUTORIAL = "KEY_SHOW_TUTORIAL";
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new FragmentSettings(), FragmentSettings.TAG)
                    .commit();
        }

        final boolean hasPermissions = PermissionCheck.hasPermissions(this, REQUIRED_PERMISSIONS);

        if (!Once.beenDone(KEY_SHOW_TUTORIAL)
                || Is.greaterThanOrEqual(MARSHMALLOW) && !hasPermissions) {

            Once.markDone(KEY_SHOW_TUTORIAL);

            Intent intent = new Intent(this, ActivityTutorial.class);
            startActivity(intent);
        }
    }

}
