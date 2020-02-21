package me.sheimi.sgit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.manichord.mgit.transport.MGitHttpConnectionFactory;

import org.eclipse.jgit.transport.CredentialsProvider;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import me.sheimi.android.utils.SecurePrefsException;
import me.sheimi.android.utils.SecurePrefsHelper;
import me.sheimi.sgit.preference.PreferenceHelper;
import timber.log.Timber;

/**
 * Custom Application Singleton
 */
public class SGitApplication extends Application {

    private static Context mContext;
    private static CredentialsProvider mCredentialsProvider;

    private SecurePrefsHelper mSecPrefs;
    private PreferenceHelper mPrefsHelper;

    static {
        MGitHttpConnectionFactory.install();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // only init Sentry if not debug build
        if (!BuildConfig.DEBUG) {
            Sentry.init(new AndroidSentryClientFactory(this));
            Log.d("SENTRY", "SENTRY Configured");
        }

        mContext = getApplicationContext();
        setAppVersionPref();
        mPrefsHelper = new PreferenceHelper(this);
        try {
            mSecPrefs = new SecurePrefsHelper(this);
            mCredentialsProvider = new AndroidJschCredentialsProvider(mSecPrefs);
        } catch (SecurePrefsException e) {
            Timber.e(e);
        }
    }


    public SecurePrefsHelper getSecurePrefsHelper() {
        return mSecPrefs;
    }

    public PreferenceHelper getPrefenceHelper() {
        return mPrefsHelper;
    }

    public static Context getContext() {
        return mContext;
    }

    private void setAppVersionPref() {
        SharedPreferences sharedPreference = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String version = BuildConfig.VERSION_NAME;
        sharedPreference
            .edit()
            .putString(getString(R.string.preference_key_app_version), version)
            .apply();
    }

    public static CredentialsProvider getJschCredentialsProvider() {
        return mCredentialsProvider;
    }
}
