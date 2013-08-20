package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.eclipse.jgit.api.Git;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class CloneDialog extends DialogFragment {

    private EditText mRemoteURL;
    private EditText mLocalPath;
    private Activity mActivity;
    private RepoUtils mRepoUtils;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        mRepoUtils = RepoUtils.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_clone, null);
        builder.setView(layout);

        mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
        mLocalPath = (EditText) layout.findViewById(R.id.localPath);

        mRemoteURL.setText(RepoUtils.TEST_REPO);

        // set button listener
        builder.setNegativeButton(getString(R.string.label_cancel),
                new CancelDialogListener());
        builder.setPositiveButton(getString(R.string.label_clone),
                new OnCloneClickedListener());

        return builder.create();
    }

    private class CancelDialogListener implements DialogInterface
            .OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            getDialog().cancel();
        }
    }

    private class OnCloneClickedListener implements DialogInterface
            .OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final String remoteURL = mRemoteURL.getText().toString();
            final String localPath = mLocalPath.getText().toString();
            ContentValues values = new ContentValues();
            values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH,
                    localPath);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL,
                    remoteURL);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_IS_CLONING,
                    RepoContract.TRUE);
            final int id = (int) RepoDbManager.getInstance(mActivity)
                    .insertRepo(values);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mRepoUtils.cloneSync(remoteURL, localPath);
                    Git git = mRepoUtils.getGit(localPath);
                    mRepoUtils.checkoutAllGranches(git);
                    mRepoUtils.updateLatestCommitInfo(git, id);
                    ContentValues values = new ContentValues();
                    values.put(RepoContract.RepoEntry.COLUMN_NAME_IS_CLONING, RepoContract.FALSE);
                    RepoDbManager.getInstance(mActivity).updateRepo(id,
                            values);
                }
            });
            thread.start();
        }
    }

}
