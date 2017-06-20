package me.sheimi.sgit.repo.tasks.repo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;
import me.sheimi.sgit.ssh.SgitTransportCallback;

public class PullTask extends RepoRemoteOpTask {

    private AsyncTaskCallback mCallback;
    private String mRemote;
    private boolean mForcePull;

    public PullTask(Repo repo, String remote, boolean forcePull, AsyncTaskCallback callback) {
        super(repo);
        mCallback = callback;
        mRemote = remote;
        mForcePull = forcePull;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = pullRepo();
        if (mCallback != null) {
            result = mCallback.doInBackground(params) & result;
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        if (mCallback != null) {
            mCallback.onProgressUpdate(progress);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mCallback != null) {
            mCallback.onPreExecute();
        }
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean pullRepo() {
        Git git;
        try {
            git = mRepo.getGit();
        } catch (StopTaskException e) {
            return false;
        }
        PullCommand pullCommand = git.pull()
                .setRemote(mRemote)
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback());

        setCredentials(pullCommand);

        try {
            String branch = null;
            if (mForcePull) {
                branch = git.getRepository().getFullBranch();
                if (!branch.startsWith("refs/heads/")) {
                    setException(new GitAPIException("not on branch") {},
                            R.string.error_pull_failed_not_on_branch);
                    return false;
                }
                branch = branch.substring(11);
                BasicProgressMonitor bpm = new BasicProgressMonitor();
                bpm.beginTask("clearing repo state", 3);

                git.getRepository().writeMergeCommitMsg(null);
                git.getRepository().writeMergeHeads(null);
                bpm.update(1);
                try {
                    git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
                } catch (Exception e) {
                }
                bpm.update(2);
                git.reset().setMode(ResetCommand.ResetType.HARD)
                        .setRef("HEAD").call();
                bpm.endTask();
            }
            pullCommand.call();
            if (mForcePull) {
                BasicProgressMonitor bpm = new BasicProgressMonitor();
                bpm.beginTask("resetting to " + mRemote + "/" + branch, 1);
                git.reset().setMode(ResetCommand.ResetType.HARD)
                        .setRef(mRemote + "/" + branch).call();
                bpm.endTask();
            }
        } catch (TransportException e) {
            setException(e);
            handleAuthError(this);
            return false;
        } catch (Exception e) {
            setException(e, R.string.error_pull_failed);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e, R.string.error_out_of_memory);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }

    @Override
    public RepoRemoteOpTask getNewTask() {
        return new PullTask(mRepo, mRemote, mForcePull, mCallback);
    }

}
