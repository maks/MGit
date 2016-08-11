package me.sheimi.sgit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/**
 *
 */
public class SGitApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        setAppVersionPref();
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

}
