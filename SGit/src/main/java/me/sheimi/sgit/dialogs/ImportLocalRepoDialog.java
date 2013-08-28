package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.jgit.api.Git;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/24/13.
 */

public class ImportLocalRepoDialog extends DialogFragment implements View.OnClickListener {

    private File mFile;
    private EditText mLocalPath;
    private ViewUtils mViewUtils;
    private RepoUtils mRepoUtils;
    private FsUtils mFsUtils;
    private Activity mActivity;

    public ImportLocalRepoDialog() {}

    public ImportLocalRepoDialog(String fromPath) {
        mFile = new File(fromPath);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        mViewUtils = ViewUtils.getInstance(getActivity());
        mRepoUtils = RepoUtils.getInstance(getActivity());
        mFsUtils = FsUtils.getInstance(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.dialog_set_local_repo_dialog));
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_import_repo, null);

        builder.setView(view);
        mLocalPath = (EditText) view.findViewById(R.id.localPath);

        // set button listener
        builder.setNegativeButton(R.string.label_cancel, new DummyDialogListener());
        builder.setPositiveButton(R.string.label_rename, new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String localPath = mLocalPath.getText().toString().trim();
        if (localPath.equals("")) {
            mViewUtils.showToastMessage(R.string.alert_field_not_empty);
            mLocalPath.setError(getString(R.string.alert_field_not_empty));
            return;
        }

        if (localPath.contains("/")) {
            mViewUtils.showToastMessage(R.string.alert_localpath_format);
            mLocalPath.setError(getString(R.string.alert_localpath_format));
            return;
        }

        final File file = mFsUtils.getRepo(localPath);

        if (file.exists()) {
            mViewUtils.showToastMessage(R.string.alert_file_exists);
            mLocalPath.setError(getString(R.string.alert_file_exists));
            return;
        }

        ContentValues values = new ContentValues();
        values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH, localPath);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL, "");
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                RepoContract.REPO_STATUS_IMPORTING);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, "");
        values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, "");

        final long id = RepoDbManager.getInstance(mActivity).insertRepo(values);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mFsUtils.copyDirectory(mFile, file);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Git git = mRepoUtils.getGit(localPath);
                        mRepoUtils.updateLatestCommitInfo(git, id);
                        ContentValues values = new ContentValues();
                        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                                RepoContract.REPO_STATUS_NULL);
                        RepoDbManager.getInstance(mActivity).updateRepo(id, values);
                    }
                });
            }
        });
        thread.start();
        dismiss();
    }
}