package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.PullTask;

public class PullAction extends RepoAction {

    public PullAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        PullTask pullTask = new PullTask(mRepo, mActivity.new ProgressCallback(
                R.string.pull_msg_init));
        pullTask.executeTask();
        mActivity.closeOperationDrawer();
    }
}
