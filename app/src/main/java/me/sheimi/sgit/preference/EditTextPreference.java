package me.sheimi.sgit.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Customised version of EditTextPreference to display current value in the summary field
 * using the same formatting markup that ListPreferences allow with %s to insert current value.
 */

public class EditTextPreference extends android.preference.EditTextPreference {

    public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (summary != null) {
            return String.format(summary.toString(), getText());
        } else {
            return summary;
        }
    }
}
