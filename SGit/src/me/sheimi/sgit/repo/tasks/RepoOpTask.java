package me.sheimi.sgit.repo.tasks;

import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.lib.ProgressMonitor;

public abstract class RepoOpTask extends SheimiAsyncTask<Void, String, Boolean> {

    protected Repo mRepo;
    protected boolean mIsTaskAdded;

    public RepoOpTask(Repo repo) {
        mRepo = repo;
        mIsTaskAdded = repo.addTask(this);
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        mRepo.removeTask();
    }

    public void executeTask() {
        if (mIsTaskAdded) {
            execute();
            return;
        }
        BasicFunctions.getActiveActivity().showToastMessage(
                R.string.error_task_running);
    }

    protected void handleAuthError(OnPasswordEntered onPassEntered) {
        String msg = mException.getMessage();

        if ((!msg.contains("Auth fail"))
                && (!msg.toLowerCase().contains("auth")))
            return;

        String errorInfo = null;
        if (msg.contains("Auth fail")) {
            errorInfo = BasicFunctions.getActiveActivity().getString(
                    R.string.dialog_prompt_for_password_title_auth_fail);
        }
        BasicFunctions.getActiveActivity().promptForPassword(onPassEntered,
                errorInfo);
    }

    class BasicProgressMonitor implements ProgressMonitor {

        private int mTotalWork;
        private int mWorkDone;

        @Override
        public void start(int i) {
        }

        @Override
        public void beginTask(String title, int totalWork) {
            mTotalWork = totalWork;
            mWorkDone = 0;
            setProgress(title, mWorkDone, mTotalWork);
        }

        @Override
        public void update(int i) {
            mWorkDone += i;
            if (mTotalWork != ProgressMonitor.UNKNOWN && mTotalWork != 0) {
                setProgress(null, mWorkDone, mTotalWork);
            }
        }

        @Override
        public void endTask() {

        }

        @Override
        public boolean isCancelled() {
            return isTaskCanceled();
        }

        private void setProgress(String title, int workDone, int totalWork) {
            String msg = title != null ? title + "..." : "";
            int showedWorkDown = Math.min(workDone, totalWork);
            int progress = 100 * showedWorkDown / totalWork;
            String rightHint = showedWorkDown + "/" + totalWork;
            String leftHint = progress + "%";
            publishProgress(msg, leftHint, rightHint,
                    Integer.toString(progress));
        }

    }

}
