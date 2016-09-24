package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;

/**
 * Created by phcoder on 05.12.15.
 */
public class ConfigRepoDialog extends SheimiDialogFragment implements
        DialogInterface.OnClickListener {

    public static final String REPO_ARG_KEY = "repo";

    private Activity mActivity;
    private EditText mGitName;
    private EditText mGitEmail;

    public ConfigRepoDialog() {}


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_repo_config, null);
        builder.setView(layout);

        mGitName = (EditText) layout.findViewById(R.id.gitName);
        mGitEmail = (EditText) layout.findViewById(R.id.gitEmail);

        StoredConfig config;
        String stored_name = "";
        String stored_email = "";

        try {
            config = ((Repo)getArguments().getSerializable(REPO_ARG_KEY)).getGit().getRepository().getConfig();
            stored_name = config.getString("user", null, "name");
            stored_email = config.getString("user", null, "email");
        } catch (StopTaskException e) {
        }
        if (stored_name == null)
            stored_name = "";
        if (stored_email == null)
            stored_email = "";
        mGitName.setText(stored_name);
        mGitEmail.setText(stored_email);

        // set button listener
        builder.setTitle(R.string.title_config_repo);
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_save, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        try {
            StoredConfig config = ((Repo)getArguments().getSerializable(REPO_ARG_KEY)).getGit().getRepository().getConfig();
            String email = mGitEmail.getText().toString();
            String name = mGitName.getText().toString();

            if (email == null || email.equals("")) {
                config.unset("user", null, "email");
            } else {
                config.setString("user", null, "email", email);
            }
            if (name == null || name.equals("")) {
                config.unset("user", null, "name");
            } else {
                config.setString("user", null, "name", name);
            }
            config.save();
        } catch (StopTaskException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}