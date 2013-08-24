package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class DeleteRepoDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private long mID;
    private String mLocalPath;
    private Activity mActivity;

    private static final String LOCAL_PATH = "local path";
    private static final String REPO_ID = "repo id";
    public DeleteRepoDialog() {}

    public DeleteRepoDialog(long id, String localPath) {
        mID = id;
        mLocalPath = localPath;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_delete_repo_title));
        builder.setMessage(getString(R.string.dialog_delete_repo_msg));

        if (savedInstanceState != null) {
            mID = savedInstanceState.getLong(REPO_ID);
            mLocalPath = savedInstanceState.getString(LOCAL_PATH);
        }

        // set button listener
        builder.setNegativeButton(getString(R.string.label_cancel), new DummyDialogListener());
        builder.setPositiveButton(getString(R.string.label_delete), this);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCAL_PATH, mLocalPath);
        outState.putLong(REPO_ID, mID);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        final File file = FsUtils.getInstance(getActivity()).getRepo(mLocalPath);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FsUtils.getInstance(getActivity()).deleteFile(file);
                RepoDbManager.getInstance(getActivity()).deleteRepo(mID);
            }
        });
        thread.start();
        if (mActivity instanceof RepoDetailActivity) {
            ActivityUtils.finishActivity(mActivity);
        }
    }

}
