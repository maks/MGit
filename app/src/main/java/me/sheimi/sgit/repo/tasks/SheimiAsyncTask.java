package me.sheimi.sgit.repo.tasks;

import android.os.AsyncTask;
import android.support.annotation.StringRes;

import me.sheimi.sgit.R;
import timber.log.Timber;

public abstract class SheimiAsyncTask<A, B, C> extends AsyncTask<A, B, C> {

    protected Throwable mException;
    protected int mErrorRes = 0;

    protected void setException(Throwable e) {
        Timber.e(e, "set exception");
        mException = e;
    }

    protected void setException(Throwable e, int errorRes) {
        Timber.e(e, "set error [%d] exception", errorRes);
        mException = e;
        mErrorRes = errorRes;
    }

    private boolean mIsCanceled = false;

    public void cancelTask() {
        mIsCanceled = true;
    }

    //TODO(kaeptmblaubaer1000): maybe make abstract?
    /**
     * This method is to be overridden and should return the resource that
     * is used as the title as the
     * {@link com.manichord.mgit.dialogs.ExceptionDialog} title when the
     * task fails with an exception.
     */
    @StringRes
    public int getErrorTitleRes() {
        return R.string.dialog_error_title;
    }

    public boolean isTaskCanceled() {
        return mIsCanceled;
    }

    public static interface AsyncTaskPostCallback {
        public void onPostExecute(Boolean isSuccess);
    }

    public static interface AsyncTaskCallback {
        public boolean doInBackground(Void... params);

        public void onPreExecute();

        public void onProgressUpdate(String... progress);

        public void onPostExecute(Boolean isSuccess);
    }
}
