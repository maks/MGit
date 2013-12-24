package me.sheimi.sgit.repo.tasks;

import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

public class CheckoutTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private String mCommitName;

    public CheckoutTask(Repo repo, String name, AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommitName = name;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = checkout(mCommitName);
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

    public boolean checkout(String name) {
        try {
            if (Repo.COMMIT_TYPE_REMOTE == Repo.getCommitType(name)) {
                checkoutFromRemote(name, Repo.getCommitName(name));
            } else {
                checkoutFromLocal(name);
            }
        } catch (GitAPIException e) {
            setException(mException);
            return false;
        } catch (JGitInternalException e) {
            setException(mException);
            // TODO LOG ERROR
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    public void checkoutFromLocal(String name) throws GitAPIException,
            JGitInternalException {
        mRepo.getGit().checkout().setName(name).call();
    }

    public void checkoutFromRemote(String remoteBranchName, String branchName)
            throws GitAPIException, JGitInternalException {
        mRepo.getGit().checkout().setCreateBranch(true).setName(branchName)
                .setStartPoint(remoteBranchName).call();
        mRepo.getGit()
                .branchCreate()
                .setUpstreamMode(
                        CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .setStartPoint(remoteBranchName).setName(branchName)
                .setForce(true).call();
    }
}
