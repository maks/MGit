package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.eclipse.jgit.api.errors.TransportException;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/24/13.
 */

public class CloneDialog extends DialogFragment implements
        View.OnClickListener, ViewUtils.OnPasswordEntered {

    private EditText mRemoteURL;
    private EditText mLocalPath;
    private EditText mUsername;
    private EditText mPassword;
    private CheckBox mIsSavePassword;
    private RepoListActivity mActivity;
    private ViewUtils mViewUtils;
    private FsUtils mFsUtils;
    private Repo mRepo;
    private RepoDbManager mRepoDbManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoListActivity) getActivity();
        mViewUtils = ViewUtils.getInstance(mActivity);
        mFsUtils = FsUtils.getInstance(mActivity);
        mRepoDbManager = RepoDbManager.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_clone, null);
        builder.setView(layout);

        mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
        mLocalPath = (EditText) layout.findViewById(R.id.localPath);
        mUsername = (EditText) layout.findViewById(R.id.username);
        mPassword = (EditText) layout.findViewById(R.id.password);
        mIsSavePassword = (CheckBox) layout.findViewById(R.id.savePassword);

        if (CommonUtils.isDebug(mActivity)) {
            mRemoteURL.setText(Repo.TEST_REPO);
            mLocalPath.setText(Repo.TEST_LOCAL);
        }

        // set button listener
        builder.setTitle(R.string.title_clone_repo);
        builder.setNegativeButton(getString(R.string.label_cancel),
                new DummyDialogListener());
        builder.setPositiveButton(getString(R.string.label_clone),
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
        String remoteURL = mRemoteURL.getText().toString().trim();
        String localPath = mLocalPath.getText().toString().trim();

        if (remoteURL.equals("")) {
            mViewUtils.showToastMessage(R.string.alert_remoteurl_required);
            mRemoteURL.setError(getString(R.string.alert_remoteurl_required));
            mRemoteURL.requestFocus();
            return;
        }
        if (localPath.equals("")) {
            mViewUtils.showToastMessage(R.string.alert_localpath_required);
            mLocalPath.setError(getString(R.string.alert_localpath_required));
            mLocalPath.requestFocus();
            return;
        }
        if (localPath.contains("/")) {
            mViewUtils.showToastMessage(R.string.alert_localpath_format);
            mLocalPath.setError(getString(R.string.alert_localpath_format));
            mLocalPath.requestFocus();
            return;
        }

        File file = mFsUtils.getRepo(localPath);
        if (file.exists()) {
            mViewUtils.showToastMessage(R.string.alert_localpath_repo_exists);
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
        long id = mRepoDbManager.insertRepo(values);
        mRepo = Repo.getRepoById(mActivity, id);

        mRepo.setUsername(username);
        mRepo.setPassword(password);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepo.clone(mActivity.getCloneMonitor(mRepo.getID()));
                    mRepo.updateLatestCommitInfo();
                    ContentValues values = new ContentValues();
                    values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                            RepoContract.REPO_STATUS_NULL);
                    RepoDbManager.getInstance(mActivity).updateRepo(
                            mRepo.getID(), values);
                } catch (TransportException e) {
                    String msg = e.getMessage();
                    if (msg.contains("Auth fail")) {
                        promptForPassword(mActivity
                                .getString(R.string.dialog_prompt_for_password_title_auth_fail));
                    } else if (msg.toLowerCase().contains("auth")) {
                        promptForPassword(null);
                    }
                    mRepo.deleteRepoSync();
                } catch (Exception e) {
                    mRepo.deleteRepoSync();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onCanceled() {
        mRepo.deleteRepo();
    }

    public void promptForPassword(final String errorInfo) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewUtils.promptForPassword(CloneDialog.this, errorInfo);
            }
        });
    }
}