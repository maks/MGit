package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.android.activities.SheimiFragmentActivity.OnEditTextDialogClicked;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.CherryPickTask;

public class CherryPickAction extends RepoAction {

    public CherryPickAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_cherrypick_title,
                R.string.dialog_cherrypick_msg_hint,
                R.string.dialog_label_cherrypick,
                new OnEditTextDialogClicked() {
                    @Override
                    public void onClicked(String text) {
                        cherrypick(text);
                    }
                });
        mActivity.closeOperationDrawer();
    }

    public void cherrypick(String commit) {
        CherryPickTask task = new CherryPickTask(mRepo, commit, new AsyncTaskPostCallback() {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                mActivity.reset();
            }
        });
        task.executeTask();
    }

}
