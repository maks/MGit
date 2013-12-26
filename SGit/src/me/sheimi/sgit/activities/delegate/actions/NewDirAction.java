package me.sheimi.sgit.activities.delegate.actions;

import me.sheimi.android.activities.SheimiFragmentActivity.OnEditTextDialogClicked;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;

public class NewDirAction extends RepoAction {

    public NewDirAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_create_dir_title,
                R.string.dialog_create_dir_hint, R.string.label_create,
                new OnEditTextDialogClicked() {
                    @Override
                    public void onClicked(String text) {
                        mActivity.getFilesFragment().newDir(text);
                    }
                });
        mActivity.closeOperationDrawer();
    }
}
