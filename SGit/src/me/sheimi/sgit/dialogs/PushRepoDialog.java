package me.sheimi.sgit.dialogs;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by sheimi on 8/16/13.
 */
public class PushRepoDialog extends SheimiDialogFragment {

    private RepoDetailActivity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (RepoDetailActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_push_repo_title));
        builder.setMessage(getString(R.string.dialog_push_repo_msg));

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_push, new PushListener());
        builder.setNeutralButton(R.string.label_push_all, new PushAllListener());

        return builder.create();
    }

    private class PushListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mActivity.pushRepo(false);
        }
    }

    private class PushAllListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mActivity.pushRepo(true);
        }
    }

}
