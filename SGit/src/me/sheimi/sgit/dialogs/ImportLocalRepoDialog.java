package me.sheimi.sgit.dialogs;

import java.io.File;

import me.sheimi.android.utils.FsUtils;
import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by sheimi on 8/24/13.
 */

public class ImportLocalRepoDialog extends SheimiDialogFragment implements
        View.OnClickListener {

    private File mFile;
    private String mFromPath;
    private Activity mActivity;
    private EditText mLocalPath;
    private static final String FROM_PATH = "from path";

    public ImportLocalRepoDialog() {
    }

    public ImportLocalRepoDialog(String fromPath) {
        mFromPath = fromPath;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        if (savedInstanceState != null) {
            String fromPath = savedInstanceState.getString(FROM_PATH);
            if (fromPath != null) {
                mFromPath = fromPath;
            }
        }
        mFile = new File(mFromPath);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.dialog_set_local_repo_dialog));
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_import_repo, null);

        builder.setView(view);
        mLocalPath = (EditText) view.findViewById(R.id.localPath);
        mLocalPath.setText(mFile.getName());

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_import,
                new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FROM_PATH, mFromPath);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = (Button) dialog
                .getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String localPath = mLocalPath.getText().toString().trim();
        if (localPath.equals("")) {
            showToastMessage(R.string.alert_field_not_empty);
            mLocalPath.setError(getString(R.string.alert_field_not_empty));
            return;
        }

        if (localPath.contains("/")) {
            showToastMessage(R.string.alert_localpath_format);
            mLocalPath.setError(getString(R.string.alert_localpath_format));
            return;
        }

        final File file = FsUtils.getRepo(localPath);

        if (file.exists()) {
            showToastMessage(R.string.alert_file_exists);
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
                FsUtils.copyDirectory(mFile, file);
                final Repo repo = Repo.getRepoById(mActivity, id);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        repo.updateLatestCommitInfo();
                        String remote = repo.getRemoteOriginURL();
                        ContentValues values = new ContentValues();
                        values.put(
                                RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL,
                                remote);
                        values.put(
                                RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                                RepoContract.REPO_STATUS_NULL);
                        RepoDbManager.getInstance(mActivity).updateRepo(id,
                                values);
                    }
                });
            }
        });
        thread.start();
        dismiss();
    }
}