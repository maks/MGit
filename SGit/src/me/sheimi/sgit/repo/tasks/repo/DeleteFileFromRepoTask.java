package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

public class DeleteFileFromRepoTask extends RepoOpTask {

    public String mFilePattern;
    public AsyncTaskPostCallback mCallback;

    public DeleteFileFromRepoTask(Repo repo, String filepattern,
            AsyncTaskPostCallback callback) {
        super(repo);
        mFilePattern = filepattern;
        mCallback = callback;
        setSuccessMsg(R.string.success_add_to_stage);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return addToStage();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean addToStage() {
        try {
            mRepo.getGit().rm().addFilepattern(mFilePattern).call();
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }
}
