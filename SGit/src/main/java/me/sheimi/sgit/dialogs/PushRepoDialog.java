package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.eclipse.jgit.api.Git;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class PushRepoDialog extends DialogFragment {

    private RepoDetailActivity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (RepoDetailActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_push_repo_title));
        builder.setMessage(getString(R.string.dialog_push_repo_msg));

        // set button listener
        builder.setNegativeButton(R.string.label_cancel, new DummyDialogListener());
        builder.setPositiveButton(R.string.label_push, new PushListener());
        builder.setNeutralButton(R.string.label_push_all, new PushAllListener());

        return builder.create();
    }

    private class PushListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // TODO
            mActivity.pushRepo(false);
        }
    }

    private class PushAllListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // TODO
            mActivity.pushRepo(true);
        }
    }

}
