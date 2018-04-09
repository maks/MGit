package me.sheimi.sgit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.manichord.mgit.transport.MGitHttpConnectionFactory;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.eclipse.jgit.transport.CredentialsProvider;

import me.sheimi.android.utils.SecurePrefsException;
import me.sheimi.android.utils.SecurePrefsHelper;
import me.sheimi.sgit.preference.PreferenceHelper;
import timber.log.Timber;

/**
 * Custom Application Singleton
 */
@ReportsCrashes(
    mailTo = "mgit@manichord.com",
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.crash_toast_text // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
)
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


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        if (!BuildConfig.DEBUG) {
            ACRA.init(this);
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
