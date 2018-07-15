package me.sheimi.sgit;

import android.util.Log;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * Provides debug-build specific Application.
 *
 * To disable Stetho console logging change the setting in src/debug/res/values/bools.xml
 */
public class MGitDebugApplication extends SGitApplication {

    private static final String LOGTAG = MGitDebugApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);

        if (true == getResources().getBoolean(R.bool.enable_stetho_timber_logging)) {
            Timber.plant(new ConfigurableStethoTree(new ConfigurableStethoTree.Configuration.Builder()
                .showTags(true)
                .minimumPriority(Log.DEBUG)
                .build()));
            Log.i(LOGTAG, "Using Stetho console logging");
        } else {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.i("Initialised Stetho");
    }
}
