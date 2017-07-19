package me.sheimi.sgit;

import android.util.Log;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.timber.StethoTree;

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

        Timber.plant(new StethoTree());
        Log.i(LOGTAG, "Using Stetho console logging");

        Timber.i("Initialised Stetho debugging");
    }

}
