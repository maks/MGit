package me.sheimi.sgit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.eclipse.jgit.transport.CredentialsProvider;

import me.sheimi.android.utils.SecurePrefsException;
import me.sheimi.android.utils.SecurePrefsHelper;
import timber.log.Timber;

/**
 *
 */
public class SGitApplication extends Application {

    private static Context mContext;
    private static CredentialsProvider mCredentialsProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //TODO: add production crash reporting
            //Timber.plant(new CrashReportingTree());
        }

        mContext = getApplicationContext();
        setAppVersionPref();
        SecurePrefsHelper secPrefs = null;
        try {
            secPrefs = new SecurePrefsHelper(this);
            mCredentialsProvider = new AndroidJschCredentialsProvider(secPrefs);
        } catch (SecurePrefsException e) {
            Timber.e(e);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    private void setAppVersionPref() {
        SharedPreferences sharedPreference = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String version = BuildConfig.VERSION_NAME;
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(getString(R.string.preference_key_app_version), version);
        editor.commit();
    }

    public static CredentialsProvider getJschCredentialsProvider() {
        return mCredentialsProvider;
    }
}
