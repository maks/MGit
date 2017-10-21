package me.sheimi.sgit.activities.delegate.actions;

import java.io.IOException;

import me.sheimi.android.activities.SheimiFragmentActivity.OnEditTextDialogClicked;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import timber.log.Timber;

public class NewFileAction extends RepoAction {

    public NewFileAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        mActivity.showEditTextDialog(R.string.dialog_create_file_title,
                R.string.dialog_create_file_hint, R.string.label_create,
                new OnEditTextDialogClicked() {
                    @Override
                    public void onClicked(String text) {
                        try {
                            mActivity.getFilesFragment().newFile(text);
                        } catch (IOException e) {
                            Timber.e(e);
                            mActivity.showMessageDialog(R.string.dialog_error_title,
                                mActivity.getString(R.string.error_something_wrong));
                        }
                    }
                });
        mActivity.closeOperationDrawer();
    }
}
