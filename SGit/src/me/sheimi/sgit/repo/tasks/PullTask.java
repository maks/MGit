package me.sheimi.sgit.repo.tasks;

import java.io.File;
import java.util.Locale;

import me.sheimi.android.utils.FsUtils;
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

public class PullTask extends RepoOpTask {

    private Repo mRepo;

    @Override
    protected Boolean doInBackground(Repo... repos) {
        mRepo = repos[0];
        boolean result = cloneRepo(mRepo);
        if (!result) {
            mRepo.deleteRepoSync();
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
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

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        mRepo.removeTask();
        if (!isSuccess && !isTaskCanceled()) {
            showError();
            return;
        }
        mRepo.updateLatestCommitInfo();
        mRepo.updateStatus(RepoContract.REPO_STATUS_NULL);
    }

    public boolean cloneRepo(Repo repo) {
        File localRepo = new File(FsUtils.getDir(FsUtils.REPO_DIR),
                repo.getLocalPath());
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repo.getRemoteURL()).setCloneAllBranches(true)
                .setProgressMonitor(new RepoCloneMonitor())
                .setTransportConfigCallback(new SgitTransportCallback())
                .setDirectory(localRepo);

        String username = repo.getUsername();
        String password = repo.getPassword();

        if (username != null && password != null && !username.equals("")
                && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    username, password);
            cloneCommand.setCredentialsProvider(auth);
        }
        try {
            cloneCommand.call();
            repo.resetGit();
        } catch (InvalidRemoteException e) {
            setException(e);
            setErrorRes(R.string.error_invalid_remote);
            return false;
        } catch (TransportException e) {
            setException(e);
            return false;
        } catch (GitAPIException e) {
            setException(e);
            setErrorRes(R.string.error_clone_failed);
            return false;
        } catch (JGitInternalException e) {
            setException(e);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e);
            setErrorRes(R.string.error_out_of_memory);
            return false;
        } catch (RuntimeException e) {
            setException(e);
            return false;
        }
        return true;
    }

    private int mTotalWork;
    private int mWorkDone;
    private String mTitle;
    
    @Override
    public void cancelTask() {
        super.cancelTask();
        mRepo.deleteRepo();
    }


    public class RepoCloneMonitor implements ProgressMonitor {

        @Override
        public void start(int totalTasks) {
            publishProgress();
        }

        @Override
        public void beginTask(String title, int totalWork) {
            mTotalWork = totalWork;
            mWorkDone = 0;
            mTitle = title;
            publishProgress();
        }

        @Override
        public void update(int i) {
            mWorkDone += i;
            publishProgress();
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
