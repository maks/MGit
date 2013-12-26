package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.android.activities.SheimiFragmentActivity.OnEditTextDialogClicked;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.CommitChangesTask;

public class CommitAction extends RepoAction {

    public CommitAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        commit();
        mActivity.closeOperationDrawer();
    }

    private void commit() {
        mActivity.showEditTextDialog(R.string.dialog_commit_title,
                R.string.dialog_commit_msg_hint, R.string.label_commit,
                new OnEditTextDialogClicked() {
                    @Override
                    public void onClicked(String text) {
                        commitChanges(text);
                    }
                });
    }

    private void commitChanges(String commitMsg) {
        CommitChangesTask commitTask = new CommitChangesTask(mRepo, commitMsg,
                new AsyncTaskPostCallback() {

                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mActivity.reset();
                    }
                });
        commitTask.executeTask();
    }
}
