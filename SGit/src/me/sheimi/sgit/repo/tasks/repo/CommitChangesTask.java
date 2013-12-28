package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.ProfileDialog;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import android.content.Context;
import android.content.SharedPreferences;

public class CommitChangesTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private String mCommitMsg;
    private boolean mIsAmend;
    private boolean mStageAll;

    public CommitChangesTask(Repo repo, String commitMsg, boolean isAmend,
            boolean stageAll, AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommitMsg = commitMsg;
        mIsAmend = isAmend;
        mStageAll = stageAll;
        setSuccessMsg(R.string.success_commit);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return commit();
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
        CommitCommand cc = mRepo.getGit().commit()
                .setCommitter(committerName, committerEmail).setAll(mStageAll)
                .setAmend(mIsAmend).setMessage(mCommitMsg);
        try {
            cc.call();
        } catch (GitAPIException e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }
}
