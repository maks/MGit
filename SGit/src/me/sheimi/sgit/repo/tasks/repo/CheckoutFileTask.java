package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

public class CheckoutFileTask extends RepoOpTask {

    private AsyncTaskPostCallback mCallback;
    private String mPath;

    public CheckoutFileTask(Repo repo, String path,
            AsyncTaskPostCallback callback) {
        super(repo);
        mCallback = callback;
        mPath = path;
        setSuccessMsg(R.string.success_checkout_file);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return checkout();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    private boolean checkout() {
        try {
            mRepo.getGit().checkout().addPath(mPath).call();
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

}
