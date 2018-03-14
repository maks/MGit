package me.sheimi.sgit.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import me.sheimi.sgit.R;

/**
 * Customised version of EditTextPreference to display current value in the summary field
 * using the same formatting markup that ListPreferences allow with %s to insert current value.
 */

public class Preference extends android.preference.Preference {

    public Preference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Preference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (summary != null) {
            SharedPreferences sharedPreference = getContext().getSharedPreferences(
                    getContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String value = sharedPreference.getString(getKey(), "");
            return String.format(summary.toString(), value);
        } else {
            return summary;
        }
    }
}
