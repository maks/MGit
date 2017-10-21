package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class MergeTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private Ref mCommit;
    private String mFFModeStr;
    private boolean mAutoCommit;

    public MergeTask(Repo repo, Ref commit, String ffModeStr,
            boolean autoCommit, AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mCommit = commit;
        mFFModeStr = ffModeStr;
        mAutoCommit = autoCommit;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return mergeBranch();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean mergeBranch() {
        String[] stringArray = BasicFunctions.getActiveActivity()
                .getResources().getStringArray(R.array.merge_ff_type);
        MergeCommand.FastForwardMode ffMode = MergeCommand.FastForwardMode.FF;
        if (mFFModeStr.equals(stringArray[1])) {
            // FF Only
            ffMode = MergeCommand.FastForwardMode.FF_ONLY;
        } else if (mFFModeStr.equals(stringArray[2])) {
            // No FF
            ffMode = MergeCommand.FastForwardMode.NO_FF;
        }
        try {
            mRepo.getGit().merge().include(mCommit).setFastForward(ffMode)
                    .call();
        } catch (GitAPIException e) {
            setException(e);
            return false;
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        if (mAutoCommit) {
            String b1 = mRepo.getBranchName();
            String b2 = mCommit.getName();
            String msg = null;
            if (b1 == null) {
                msg = String.format("Merge branch '%s'",
                        Repo.getCommitDisplayName(b2));
            } else {
                msg = String.format("Merge branch '%s' into %s",
                        Repo.getCommitDisplayName(b2),
                        Repo.getCommitDisplayName(b1));
            }
            try {
                CommitChangesTask.commit(mRepo, false, false, msg, null, null);
            } catch (GitAPIException e) {
                setException(e);
                return false;
            } catch (StopTaskException e) {
                return false;
            } catch (Throwable e) {
                setException(e);
                return false;
            }
        }
        mRepo.updateLatestCommitInfo();
        return true;
    }
}
