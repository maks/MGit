package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.ProfileDialog;

import org.eclipse.jgit.api.errors.GitAPIException;

import android.content.Context;
import android.content.SharedPreferences;

public class CommitChangesTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private String mCommitMsg;

    public CommitChangesTask(Repo repo, String commitMsg,
            AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommitMsg = commitMsg;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = commit();
        if (!result) {
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean commit() {
        SharedPreferences sharedPreferences = BasicFunctions
                .getActiveActivity().getSharedPreferences(
                        BasicFunctions.getActiveActivity().getString(
                                R.string.preference_file_key),
                        Context.MODE_PRIVATE);
        String committerName = sharedPreferences.getString(
                ProfileDialog.GIT_USER_NAME, "");
        String committerEmail = sharedPreferences.getString(
                ProfileDialog.GIT_USER_EMAIL, "");
        try {
            mRepo.getGit().add().addFilepattern(".").call();
            mRepo.getGit().commit().setMessage(mCommitMsg)
                    .setCommitter(committerName, committerEmail).setAll(true)
                    .call();
        } catch (GitAPIException e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }
}
