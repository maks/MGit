package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.io.File;

import me.sheimi.android.utils.Profile;
import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.SGitApplication;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.databinding.DialogCloneBinding;
import me.sheimi.sgit.preference.PreferenceHelper;
import me.sheimi.sgit.repo.tasks.repo.CloneTask;

/**
 * Dialog UI used to perform clone operation
 */

public class CloneDialog extends SheimiDialogFragment implements View.OnClickListener {

    private RepoListActivity mActivity;
    private Repo mRepo;
    private DialogCloneBinding mBinding;
    private PreferenceHelper mPrefsHelper;

    private class RemoteUrlFocusListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                final String remoteUrl = mBinding.remoteURL.getText().toString();
                String localDefault = stripUrlFromRepo(remoteUrl);
                localDefault = stripGitExtension(localDefault);
                if (!localDefault.equals("")) {
                    mBinding.localPath.setText(localDefault);
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

        mPrefsHelper = ((SGitApplication) mActivity.getApplicationContext()).getPrefenceHelper();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();

        mBinding = DialogCloneBinding.inflate(inflater);
        builder.setView(mBinding.getRoot());

        if (Profile.hasLastCloneFailed())
            fillInformationFromPreviousCloneFail(Profile.getLastCloneTryRepo());

        mBinding.remoteURL.setOnFocusChangeListener(new RemoteUrlFocusListener());

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
        mBinding.remoteURL.setText(lastCloneTryRepo.getRemoteURL());
        mBinding.localPath.setText(lastCloneTryRepo.getLocalPath());
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
        String remoteURL = mBinding.remoteURL.getText().toString().trim();
        String localPath = mBinding.localPath.getText().toString().trim();

        if (remoteURL.equals("")) {
            showToastMessage(R.string.alert_remoteurl_required);
            mBinding.remoteURL.setError(getString(R.string.alert_remoteurl_required));
            mBinding.remoteURL.requestFocus();
            return;
        }
        if (localPath.isEmpty()) {
            showToastMessage(R.string.alert_localpath_required);
            mBinding.localPath.setError(getString(R.string.alert_localpath_required));
            mBinding.localPath.requestFocus();
            return;
        }
        if (localPath.contains("/")) {
            showToastMessage(R.string.alert_localpath_format);
            mBinding.localPath.setError(getString(R.string.alert_localpath_format));
            mBinding.localPath.requestFocus();
            return;
        }

        // If user is accepting the default path in the hint, we need to set localPath to
        // the string in the hint, so that the following checks don't fail.
        if (mBinding.localPath.getHint().toString() != getString(R.string.dialog_clone_local_path_hint)) {
            localPath = mBinding.localPath.getHint().toString();
        }
        File file = Repo.getDir(mPrefsHelper, localPath);
        if (file.exists()) {
            showToastMessage(R.string.alert_localpath_repo_exists);
            mBinding.localPath.setError(getString(R.string.alert_localpath_repo_exists));
            mBinding.localPath.requestFocus();
            return;
        }

        cloneRepo();
        dismiss();
    }

    public void cloneRepo() {
        String remoteURL = mBinding.remoteURL.getText().toString().trim();
        String localPath = mBinding.localPath.getText().toString().trim();

        mRepo = Repo.createRepo(localPath, remoteURL, getString(R.string.cloning));

        CloneTask task = new CloneTask(mRepo, mBinding.cloneRecursive.isChecked(), getString(R.string.cloning), null);
        task.executeTask();
    }
}
