package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

public class CheckoutTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private String mCommitName;
    private boolean mCreateNewBranch;

    public CheckoutTask(Repo repo, String name, boolean createNewBranch,AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommitName = name;
        mCreateNewBranch = createNewBranch;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return checkout(mCommitName,mCreateNewBranch);
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean checkout(String name,boolean createNewBranch) {
        try {
            if (createNewBranch) {
                checkoutNewBranch(name);
            }else {
                if (Repo.COMMIT_TYPE_REMOTE == Repo.getCommitType(name)) {
                    checkoutFromRemote(name, Repo.getCommitName(name));
                } else {
                    checkoutFromLocal(name);
                }
            }
        } catch (StopTaskException e) {
            return false;
        } catch (GitAPIException e) {
            setException(mException);
            return false;
        } catch (JGitInternalException e) {
            setException(mException);
            return false;
        } catch (Throwable e) {
            setException(mException);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    public void checkoutNewBranch(String name) throws GitAPIException,
            JGitInternalException, StopTaskException {
        mRepo.getGit().checkout().setName(name).setCreateBranch(true).call();
    }

    public void checkoutFromLocal(String name) throws GitAPIException,
            JGitInternalException, StopTaskException {
        mRepo.getGit().checkout().setName(name).call();
    }

    public void checkoutFromRemote(String remoteBranchName, String branchName)
            throws GitAPIException, JGitInternalException, StopTaskException {
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
