package me.sheimi.sgit.activities.delegate.actions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import java.util.ArrayList;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.repo.tasks.repo.FetchTask;

public class FetchAction extends RepoAction {
    public FetchAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
        remotes = new ArrayList<String>();
    }

    private ArrayList<String> remotes;

    @Override
    public void execute() {
        fetchDialog().show();
        mActivity.closeOperationDrawer();
    }

    private void fetch() {
        final String[] remotesArray = remotes.toArray(new String[0]);
        final FetchTask fetchTask = new FetchTask(remotesArray, mRepo, mActivity.new ProgressCallback(R.string.fetch_msg_init));
        fetchTask.executeTask();
    }

    private Dialog fetchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final String[] originRemotes = mRepo.getRemotes().toArray(new String[0]);
        return builder.setTitle(R.string.dialog_fetch_title)
                .setMultiChoiceItems(originRemotes, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index, boolean isChecked) {
                        if (isChecked) {
                            remotes.add(originRemotes[index]);
                        } else {
                            for (int i = 0; i < remotes.size(); ++i) {
                                if (remotes.get(i) == originRemotes[index]) {
                                    remotes.remove(i);
                                }
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.dialog_fetch_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fetch();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DummyDialogListener())
                .create();
    }
}
