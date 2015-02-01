package me.sheimi.sgit.dialogs;

import me.sheimi.android.utils.Profile;
import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Activity mActivity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_profile, null);
        builder.setView(layout);

        mGitName = (EditText) layout.findViewById(R.id.gitName);
        mGitEmail = (EditText) layout.findViewById(R.id.gitEmail);

        String stored_name = Profile.getUsername();
        String stored_email = Profile.getEmail();
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

        Profile.setProfileInformation(name, email);
    }
}