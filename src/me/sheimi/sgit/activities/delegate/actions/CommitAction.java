package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.CommitChangesTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CommitAction extends RepoAction {

    public CommitAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        commit();
        mActivity.closeOperationDrawer();
    }

    private void commit(String commitMsg, boolean isAmend, boolean stageAll) {
        CommitChangesTask commitTask = new CommitChangesTask(mRepo, commitMsg,
                isAmend, stageAll, new AsyncTaskPostCallback() {

                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mActivity.reset();
                    }
                });
        commitTask.executeTask();
    }

    private void commit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_commit, null);
        final EditText commitMsg = (EditText) layout
                .findViewById(R.id.commitMsg);
        final CheckBox isAmend = (CheckBox) layout.findViewById(R.id.isAmend);
        final CheckBox autoStage = (CheckBox) layout
                .findViewById(R.id.autoStage);
        isAmend.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked) {
                    commitMsg.setText(mRepo.getLastCommitMsg());
                } else {
                    commitMsg.setText("");
                }
            }
        });
        builder.setTitle(R.string.dialog_commit_title)
                .setView(layout)
                .setPositiveButton(R.string.dialog_commit_positive_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                String msg = commitMsg.getText().toString();
                                boolean amend = isAmend.isChecked();
                                boolean stage = autoStage.isChecked();
                                commit(msg, amend, stage);
                            }
                        })
                .setNegativeButton(R.string.label_cancel,
                        new DummyDialogListener()).show();
    }
}
