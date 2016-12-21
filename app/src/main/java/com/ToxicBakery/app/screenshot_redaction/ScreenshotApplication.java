package com.ToxicBakery.app.screenshot_redaction;

import android.Manifest;
import android.app.Application;
import android.util.Log;

import com.ToxicBakery.android.version.Is;
import com.ToxicBakery.app.screenshot_redaction.copy.CopyToSdCard;
import com.ToxicBakery.app.screenshot_redaction.copy.TessDataRawResourceCopyConfiguration;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglish;
import com.ToxicBakery.app.screenshot_redaction.dictionary.impl.DictionaryEnglishNames;
import com.ToxicBakery.app.screenshot_redaction.notification.ScreenShotNotifications;
import com.ToxicBakery.app.screenshot_redaction.service.ScreenshotService;
import com.ToxicBakery.app.screenshot_redaction.util.PermissionCheck;

import java.io.File;

import jonathanfinerty.once.Once;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

import static com.ToxicBakery.android.version.SdkVersion.MARSHMALLOW;

public class ScreenshotApplication extends Application {

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String TAG = "ScreenshotApplication";
    private static final String REMOVE_OLD_DIR = "REMOVE_OLD_DIR";

    private ScreenShotNotifications screenShotNotifications;

    @Override
    public void onCreate() {
        super.onCreate();

        // Install Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Once.initialise(this);
        screenShotNotifications = new ScreenShotNotifications(this);

        Observable.just(true)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        initializeResources();
                    }
                });
    }

    /**
     * Gets the screenshot notification instance from the process application.
     *
     * @return screenshot notification instance
     */
    public ScreenShotNotifications getScreenShotNotifications() {
        return screenShotNotifications;
    }

    void initializeResources() {
        if (!Once.beenDone(REMOVE_OLD_DIR)) {
            Once.markDone(REMOVE_OLD_DIR);

            File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
            if (externalFilesDir != null
                    && externalFilesDir.exists()
                    && !externalFilesDir.delete()) {

                Log.e(TAG, "Failed to delete external storage directory");
            }
        }

        if (Is.lessThan(MARSHMALLOW) || PermissionCheck.hasPermissions(getApplicationContext(), PERMISSIONS)) {
            new CopyToSdCard()
                    .copy(new TessDataRawResourceCopyConfiguration(getApplicationContext(), R.raw.eng));

            DictionaryEnglish.getInstance(getApplicationContext());
            DictionaryEnglishNames.getInstance(getApplicationContext());
            ScreenshotService.startScreenshotService(getApplicationContext());
        }
    }

}
