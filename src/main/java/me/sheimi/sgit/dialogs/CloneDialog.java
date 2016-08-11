package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;

import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import me.sheimi.android.utils.Profile;
import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.CloneTask;

/**
 * Created by sheimi on 8/24/13.
 */

public class CloneDialog extends SheimiDialogFragment implements
        View.OnClickListener, OnPasswordEntered {

    private EditText mRemoteURL;
    private EditText mLocalPath;
    private EditText mUsername;
    private EditText mPassword;
    private CheckBox mIsSavePassword;
    private CheckBox mCloneRecursive;
    private RepoListActivity mActivity;
    private Repo mRepo;

    private class RemoteUrlFocusListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                final String remoteUrl = mRemoteURL.getText().toString();
                String localDefault = stripUrlFromRepo(remoteUrl);
                localDefault = stripGitExtension(localDefault);
                if (!localDefault.equals("")) {
                    mLocalPath.setText(localDefault);
                }
            }
        }

        private String stripUrlFromRepo(final String remoteUrl) {
            final int lastSlash = remoteUrl.lastIndexOf("/");
            if (lastSlash != -1) {
                return remoteUrl.substring(lastSlash + 1);
            }

            return remoteUrl;
        }

        private String stripGitExtension(final String remoteUrl) {
            final int extension = remoteUrl.indexOf(".git");
            if (extension != -1) {
                return remoteUrl.substring(0, extension);
            }

            return remoteUrl;
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoListActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_clone, null);
        builder.setView(layout);

        mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
        mLocalPath = (EditText) layout.findViewById(R.id.localPath);
        mUsername = (EditText) layout.findViewById(R.id.username);
        mPassword = (EditText) layout.findViewById(R.id.password);
        mIsSavePassword = (CheckBox) layout.findViewById(R.id.savePassword);
        mCloneRecursive = (CheckBox) layout.findViewById(R.id.cloneRecursive);

        if ( Profile.hasLastCloneFailed() )
            fillInformationFromPreviousCloneFail( Profile.getLastCloneTryRepo() );

        mRemoteURL.setOnFocusChangeListener(new RemoteUrlFocusListener());

        // set button listener
        builder.setTitle(R.string.title_clone_repo);
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setNeutralButton(R.string.dialog_clone_neutral_label,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InitDialog id = new InitDialog();
                        id.show(getFragmentManager(), "init-dialog");
                    }
                });
        builder.setPositiveButton(R.string.label_clone,
                new DummyDialogListener());

        return builder.create();
    }

    private void fillInformationFromPreviousCloneFail(Repo lastCloneTryRepo) {
        mRemoteURL.setText( lastCloneTryRepo.getRemoteURL() );
        mLocalPath.setText( lastCloneTryRepo.getLocalPath() );
        mUsername.setText( lastCloneTryRepo.getUsername() );
        mPassword.setText( lastCloneTryRepo.getPassword() );
        if ( lastCloneTryRepo.getUsername().equals("") && lastCloneTryRepo.getPassword().equals(""))
            mIsSavePassword.setChecked(false);
        else
            mIsSavePassword.setChecked(true);
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
        String remoteURL = mRemoteURL.getText().toString().trim();
        String localPath = mLocalPath.getText().toString().trim();

        if (remoteURL.equals("")) {
            showToastMessage(R.string.alert_remoteurl_required);
            mRemoteURL.setError(getString(R.string.alert_remoteurl_required));
            mRemoteURL.requestFocus();
            return;
        }
        if (localPath.isEmpty()) {
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

        // If user is accepting the default path in the hint, we need to set localPath to
        // the string in the hint, so that the following checks don't fail.
        if (mLocalPath.getHint().toString() != getString(R.string.dialog_clone_local_path_hint)) {
            localPath = mLocalPath.getHint().toString();
        }
        File file = Repo.getDir(getActivity(), localPath);
        if (file.exists()) {
            showToastMessage(R.string.alert_localpath_repo_exists);
            mLocalPath
                    .setError(getString(R.string.alert_localpath_repo_exists));
            mLocalPath.requestFocus();
            return;
        }

        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        boolean savePassword = mIsSavePassword.isChecked();
        onClicked(username, password, savePassword);
        dismiss();
    }

    public void cloneRepo() {
        onClicked(null, null, false);
    }

    @Override
    public void onClicked(String username, String password, boolean savePassword) {
        String remoteURL = mRemoteURL.getText().toString().trim();
        String localPath = mLocalPath.getText().toString().trim();
        ContentValues values = new ContentValues();
        values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH, localPath);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL, remoteURL);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                RepoContract.REPO_STATUS_WAITING_CLONE);
        if (savePassword) {
            values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, username);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, password);
        } else {
            values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, "");
            values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, "");
        }
        long id = RepoDbManager.insertRepo(values);
        mRepo = Repo.getRepoById(mActivity, id);

        mRepo.setUsername(username);
        mRepo.setPassword(password);

        CloneTask task = new CloneTask(mRepo, this, mCloneRecursive.isChecked());
        task.executeTask();

    }

    @Override
    public void onCanceled() {
        mRepo.deleteRepo();
    }
}