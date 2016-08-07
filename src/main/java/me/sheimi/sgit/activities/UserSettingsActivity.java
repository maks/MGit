package me.sheimi.sgit.activities;

import android.app.Activity;
import android.os.Bundle;

import me.sheimi.sgit.fragments.SettingsFragment;

/**
 * Activity for user settings
 */
public class UserSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
