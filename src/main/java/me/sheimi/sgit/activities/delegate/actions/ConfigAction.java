package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.activities.ViewFileActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.ConfigRepoDialog;

/**
 * Created by phcoder on 05.12.15.
 */
public class ConfigAction extends RepoAction {


    public ConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        ConfigRepoDialog configDialog = new ConfigRepoDialog(mRepo);
        configDialog.show(mActivity.getFragmentManager(), "repo-config-dialog");
    }

}
