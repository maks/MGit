package me.sheimi.sgit.repo.tasks.repo;

import android.content.Context;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;

import me.sheimi.android.utils.Profile;
import me.sheimi.sgit.R;
import me.sheimi.sgit.SGitApplication;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;

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
        try {
            commit(mRepo, mStageAll, mIsAmend, mCommitMsg);
        } catch (StopTaskException e) {
            return false;
        } catch (GitAPIException e) {
            setException(e);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    public static void commit(Repo repo, boolean stageAll, boolean isAmend,
            String msg) throws Exception, NoHeadException, NoMessageException,
            UnmergedPathsException, ConcurrentRefUpdateException,
            WrongRepositoryStateException, GitAPIException, StopTaskException {
        Context context = SGitApplication.getContext();
        String committerName = Profile.getUsername(context);
        String committerEmail = Profile.getEmail(context);
        if (committerName == "" || committerEmail == "") {
            throw new Exception("Please set your name and email");
        }
        CommitCommand cc = repo.getGit().commit()
                .setCommitter(committerName, committerEmail).setAll(stageAll)
                .setAmend(isAmend).setMessage(msg);
        cc.call();
        repo.updateLatestCommitInfo();
    }
}
