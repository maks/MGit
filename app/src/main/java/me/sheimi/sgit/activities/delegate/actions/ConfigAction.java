package me.sheimi.sgit.activities.delegate.actions;

import android.app.AlertDialog;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.GitConfig;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.databinding.DialogRepoConfigBinding;
import me.sheimi.sgit.exception.StopTaskException;
import timber.log.Timber;

/**
 * Action to display configuration for a Repo
 */
public class ConfigAction extends RepoAction {


    public ConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {

        try {
            DialogRepoConfigBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.dialog_repo_config, null, false);
            GitConfig gitConfig = new GitConfig(mRepo);
            binding.setViewModel(gitConfig);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setView(binding.getRoot())
                .setNeutralButton(R.string.label_done, null)
                .create().show();

        } catch (StopTaskException e) {
            //FIXME: show error to user
            Timber.e(e);
        }
    }

}
