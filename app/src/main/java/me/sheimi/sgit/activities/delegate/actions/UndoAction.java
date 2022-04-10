package me.sheimi.sgit.activities.delegate.actions;

import android.content.DialogInterface;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Iterator;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.UndoCommitTask;

public class UndoAction extends RepoAction {

    public UndoAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        boolean firstCommit = true;
        boolean noCommit = true;
        try {
            Iterator<RevCommit> logIt = mRepo.getGit().log().call().iterator();
            noCommit = !logIt.hasNext();
            if (!noCommit) {
                logIt.next();
                firstCommit = !logIt.hasNext();
            }
        } catch (GitAPIException | StopTaskException e) {
            e.printStackTrace();
        }
        if (noCommit) {
            mActivity.showMessageDialog(R.string.dialog_undo_commit_title, R.string.dialog_undo_no_commit_msg);
        } else if (firstCommit) {
            mActivity.showMessageDialog(R.string.dialog_undo_commit_title, R.string.dialog_undo_first_commit_msg);
        } else {
            mActivity.showMessageDialog(R.string.dialog_undo_commit_title,
                R.string.dialog_undo_commit_msg, R.string.action_undo,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        undo();
                    }
                });
        }
        mActivity.closeOperationDrawer();
    }

    public void undo() {
        UndoCommitTask undoTask = new UndoCommitTask(mRepo,
            new AsyncTaskPostCallback() {
                @Override
                public void onPostExecute(Boolean isSuccess) {
                    mActivity.reset();
                }
            });
        undoTask.executeTask();
    }
}
