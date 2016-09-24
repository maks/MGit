package me.sheimi.sgit.activities.delegate.actions;

import android.os.Bundle;

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

        Bundle args = new Bundle();
        args.putSerializable(ConfigRepoDialog.REPO_ARG_KEY, mRepo);
        ConfigRepoDialog configDialog = new ConfigRepoDialog();
        configDialog.setArguments(args);
        configDialog.show(mActivity.getFragmentManager(), "repo-config-dialog");
    }

}
