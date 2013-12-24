package me.sheimi.sgit.dialogs;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by sheimi on 8/24/13.
 */

public class ProfileDialog extends SheimiDialogFragment implements
        DialogInterface.OnClickListener {

    private EditText mGitName;
    private EditText mGitEmail;
    private Activity mActivity;
    private SharedPreferences mSharedPreference;

    public static final String GIT_USER_NAME = "user.name";
    public static final String GIT_USER_EMAIL = "user.email";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        mSharedPreference = mActivity.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_profile, null);
        builder.setView(layout);

        mGitName = (EditText) layout.findViewById(R.id.gitName);
        mGitEmail = (EditText) layout.findViewById(R.id.gitEmail);
        String stored_name = mSharedPreference.getString(GIT_USER_NAME, "");
        String stored_email = mSharedPreference.getString(GIT_USER_EMAIL, "");
        mGitName.setText(stored_name);
        mGitEmail.setText(stored_email);

        // set button listener
        builder.setTitle(R.string.dialog_profile_git_profile_title);
        builder.setNegativeButton(getString(R.string.label_cancel),
                new DummyDialogListener());
        builder.setPositiveButton(getString(R.string.label_done), this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        String email = mGitEmail.getText().toString();
        String name = mGitName.getText().toString();
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(GIT_USER_NAME, name);
        editor.putString(GIT_USER_EMAIL, email);
        editor.commit();
    }
}