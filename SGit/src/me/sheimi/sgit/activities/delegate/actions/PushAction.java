package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.repo.tasks.repo.PushTask;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class PushAction extends RepoAction {

    public PushAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        showPushRepoDialog();
        mActivity.closeOperationDrawer();
    }

    public void push(boolean pushAll) {
        PushTask pushTask = new PushTask(mRepo, pushAll,
                mActivity.new ProgressCallback(R.string.push_msg_init));
        pushTask.executeTask();
    }

    public void showPushRepoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.dialog_push_repo_title));
        builder.setMessage(mActivity.getString(R.string.dialog_push_repo_msg));

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_push, new PushListener());
        builder.setNeutralButton(R.string.label_push_all, new PushAllListener());
        builder.show();
    }

    private class PushListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            push(false);
        }
    }

    private class PushAllListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            push(true);
        }
    }

}
