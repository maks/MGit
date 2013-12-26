package me.sheimi.sgit.activities.delegate.actions;

import android.content.DialogInterface;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.ResetCommitTask;

public class ResetAction extends RepoAction {

    public ResetAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showMessageDialog(R.string.dialog_reset_commit_title,
                R.string.dialog_reset_commit_msg, R.string.action_reset,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reset();
                    }
                });
        mActivity.closeOperationDrawer();
    }

    public void reset() {
        ResetCommitTask resetTask = new ResetCommitTask(mRepo,
                new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mActivity.reset();
                    }
                });
        resetTask.executeTask();
    }
}
