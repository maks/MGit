package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;

public class DiffAction extends RepoAction {

    public DiffAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.enterDiffActionMode();
        mActivity.closeOperationDrawer();
    }
}
