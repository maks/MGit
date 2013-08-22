package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class DeleteRepoDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private long mID;
    private RepoUtils mRepoUtils;
    private String mLocalPath;
    private View.OnClickListener mOnDeleteClickListener;

    public DeleteRepoDialog(long id, String localPath, View.OnClickListener listener) {
        mID = id;
        mLocalPath = localPath;
        mOnDeleteClickListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.dialog_delete_repo_title));
        builder.setMessage(getString(R.string.dialog_delete_repo_msg));

        // set button listener
        builder.setNegativeButton(getString(R.string.label_cancel), new DummyDialogListener());
        builder.setPositiveButton(getString(R.string.label_delete), this);

        return builder.create();
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
        if (mOnDeleteClickListener != null) {
            mOnDeleteClickListener.onClick(null);
        }
    }

}
