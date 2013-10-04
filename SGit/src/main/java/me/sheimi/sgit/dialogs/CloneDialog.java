package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/24/13.
 */

public class CloneDialog extends DialogFragment implements View.OnClickListener {

    private EditText mRemoteURL;
    private EditText mLocalPath;
    private EditText mUsername;
    private EditText mPassword;
    private RepoListActivity mActivity;
    private RepoUtils mRepoUtils;
    private ViewUtils mViewUtils;
    private FsUtils mFsUtils;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoListActivity) getActivity();
        mRepoUtils = RepoUtils.getInstance(mActivity);
        mViewUtils = ViewUtils.getInstance(mActivity);
        mFsUtils = FsUtils.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_clone, null);
        builder.setView(layout);

        mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
        mLocalPath = (EditText) layout.findViewById(R.id.localPath);
        mUsername = (EditText) layout.findViewById(R.id.username);
        mPassword = (EditText) layout.findViewById(R.id.password);

        if (CommonUtils.isDebug(mActivity)) {
            mRemoteURL.setText(RepoUtils.TEST_REPO);
            mLocalPath.setText(RepoUtils.TEST_LOCAL);
            mUsername.setText(RepoUtils.TEST_USERNAME);
            mPassword.setText(RepoUtils.TEST_PASSWORD);
        }

        // set button listener
        builder.setTitle(R.string.title_clone_repo);
        builder.setNegativeButton(getString(R.string.label_cancel), new DummyDialogListener());
        builder.setPositiveButton(getString(R.string.label_clone), new DummyDialogListener());

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
            mLocalPath.setError(getString(R.string.alert_localpath_repo_exists));
            mLocalPath.requestFocus();
            return;
        }

        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        ContentValues values = new ContentValues();
        values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH, localPath);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL, remoteURL);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                RepoContract.REPO_STATUS_WAITING_CLONE);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, username);
        values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, password);
        long id = RepoDbManager.getInstance(mActivity)
                .insertRepo(values);
        cloneRepo(id, remoteURL, localPath, username, password);
        dismiss();
    }

    public void cloneRepo(final long id, final String remoteUrl, final String localPath,
                          final String username, final String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRepoUtils.cloneSync(remoteUrl, localPath, username, password,
                            mActivity.getCloneMonitor(id));
                    Git git = mRepoUtils.getGit(localPath);
                    mRepoUtils.updateLatestCommitInfo(git, id);
                    ContentValues values = new ContentValues();
                    values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                            RepoContract.REPO_STATUS_NULL);
                    RepoDbManager.getInstance(mActivity).updateRepo(id, values);
                } catch (GitAPIException e) {
                    RepoDbManager.getInstance(mActivity).deleteRepo(id);
                } catch (JGitInternalException e) {
                    RepoDbManager.getInstance(mActivity).deleteRepo(id);
                } catch (OutOfMemoryError e) {
                    RepoDbManager.getInstance(mActivity).deleteRepo(id);
                }
            }
        });
        thread.start();
    }

}