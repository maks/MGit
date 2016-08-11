package me.sheimi.sgit.dialogs;

import java.io.File;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.InitLocalTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by sheimi on 8/24/13.
 */

public class InitDialog extends SheimiDialogFragment implements
        View.OnClickListener {

    private EditText mLocalPath;
    private RepoListActivity mActivity;
    private Repo mRepo;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoListActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_init_repo, null);
        builder.setView(layout);

        mLocalPath = (EditText) layout.findViewById(R.id.localPath);

        // set button listener
        builder.setTitle(R.string.dialog_init_repo_title);
        builder.setNegativeButton(getString(R.string.label_cancel),
                new DummyDialogListener());
        builder.setPositiveButton(
                getString(R.string.dialog_init_repo_positive_label),
                new DummyDialogListener());

        return builder.create();
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
        String localPath = mLocalPath.getText().toString().trim();

        if (localPath.equals("")) {
            showToastMessage(R.string.alert_localpath_required);
            mLocalPath.setError(getString(R.string.alert_localpath_required));
            mLocalPath.requestFocus();
            return;
        }
        if (localPath.contains("/")) {
            showToastMessage(R.string.alert_localpath_format);
            mLocalPath.setError(getString(R.string.alert_localpath_format));
            mLocalPath.requestFocus();
            return;
        }

        File file = Repo.getDir(getActivity(), localPath);
        if (file.exists()) {
            showToastMessage(R.string.alert_localpath_repo_exists);
            mLocalPath
                    .setError(getString(R.string.alert_localpath_repo_exists));
            mLocalPath.requestFocus();
            return;
        }

        localPath = mLocalPath.getText().toString().trim();
        ContentValues values = new ContentValues();
        values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH, localPath);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL, "local repository");
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                RepoContract.REPO_STATUS_INITING);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, "");
        values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, "");
        long id = RepoDbManager.insertRepo(values);
        mRepo = Repo.getRepoById(mActivity, id);

        InitLocalTask task = new InitLocalTask(mRepo);
        task.executeTask();

        dismiss();
    }

}