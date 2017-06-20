package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.database.models.Repo;

/**
 * Super class for Tasks that operate on a git remote
 */

public abstract class RepoRemoteOpTask extends RepoOpTask implements SheimiFragmentActivity.OnPasswordEntered {


    public RepoRemoteOpTask(Repo repo) {
        super(repo);
    }


    @Override
    public void onClicked(String username, String password, boolean savePassword) {
        mRepo.setUsername(username);
        mRepo.setPassword(password);
        if (savePassword) {
            mRepo.saveCredentials();
        }

        mRepo.removeTask(this);
        getNewTask().executeTask();
    }

    @Override
    public void onCanceled() {

    }

    public abstract RepoRemoteOpTask getNewTask();
}
