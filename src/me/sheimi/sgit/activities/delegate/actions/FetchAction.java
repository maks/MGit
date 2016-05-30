package me.sheimi.sgit.activities.delegate.actions;

import android.util.Log;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.FetchTask;

public class FetchAction extends RepoAction {
    public FetchAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);

        Log.d("SGit Fetch Action", "Added FetchAction!");
    }

    @Override
    public void execute() {
        FetchTask fetchTask = new FetchTask(mRepo, mActivity.new ProgressCallback(R.string.fetch_msg_init));
        fetchTask.executeTask();
        mActivity.closeOperationDrawer();
    }
}
