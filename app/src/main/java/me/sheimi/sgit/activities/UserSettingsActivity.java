package me.sheimi.sgit.activities;

import android.os.Bundle;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.fragments.SettingsFragment;

/**
 * Activity for user settings
 */
public class UserSettingsActivity extends SheimiFragmentActivity {

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
