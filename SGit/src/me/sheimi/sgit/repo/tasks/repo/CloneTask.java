package me.sheimi.sgit.repo.tasks.repo;

import java.io.File;
import java.util.Locale;

import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.ssh.SgitTransportCallback;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class CloneTask extends RepoOpTask {

    private OnPasswordEntered mOnPasswordEnter;

    public CloneTask(Repo repo, OnPasswordEntered onPasswordEnter) {
        super(repo);
        mOnPasswordEnter = onPasswordEnter;
    }

    @Override
    protected Boolean doInBackground(Void... v) {
        boolean result = cloneRepo();
        if (!result) {
            mRepo.deleteRepoSync();
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (isTaskCanceled()) {
            return;
        }
        if (isSuccess) {
            mRepo.updateLatestCommitInfo();
            mRepo.updateStatus(RepoContract.REPO_STATUS_NULL);
        }
    }

    public boolean cloneRepo() {
        File localRepo = mRepo.getDir();
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(mRepo.getRemoteURL()).setCloneAllBranches(true)
                .setProgressMonitor(new RepoCloneMonitor())
                .setTransportConfigCallback(new SgitTransportCallback())
                .setDirectory(localRepo);

        String username = mRepo.getUsername();
        String password = mRepo.getPassword();

        if (username != null && password != null && !username.equals("")
                && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    username, password);
            cloneCommand.setCredentialsProvider(auth);
        }
        try {
            cloneCommand.call();
        } catch (InvalidRemoteException e) {
            setException(e, R.string.error_invalid_remote);
            return false;
        } catch (TransportException e) {
            setException(e);
            handleAuthError(mOnPasswordEnter);
            return false;
        } catch (GitAPIException e) {
            setException(e, R.string.error_clone_failed);
            return false;
        } catch (JGitInternalException e) {
            setException(e);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e, R.string.error_out_of_memory);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

    @Override
    public void cancelTask() {
        super.cancelTask();
        mRepo.deleteRepo();
    }

    public class RepoCloneMonitor implements ProgressMonitor {

        private int mTotalWork;
        private int mWorkDone;
        private String mTitle;

        private void publishProgressInner() {
            String status = "";
            String percent = "";
            if (mTitle != null) {
                status = String.format(Locale.getDefault(), "%s ... ", mTitle);
                percent = "0%";
            }
            if (mTotalWork != 0) {
                int p = 100 * mWorkDone / mTotalWork;
                percent = String.format(Locale.getDefault(), "(%d%%)", p);
            }
            mRepo.updateStatus(status + percent);
        }

        @Override
        public void start(int totalTasks) {
            publishProgressInner();
        }

        @Override
        public void beginTask(String title, int totalWork) {
            mTotalWork = totalWork;
            mWorkDone = 0;
            mTitle = title;
            publishProgressInner();
        }

        @Override
        public void update(int i) {
            mWorkDone += i;
            publishProgressInner();
        }

        @Override
        public void endTask() {
        }

        @Override
        public boolean isCancelled() {
            return isTaskCanceled();
        }

    }

}
