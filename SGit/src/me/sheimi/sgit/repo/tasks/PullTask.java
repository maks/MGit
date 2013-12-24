package me.sheimi.sgit.repo.tasks;

import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.ssh.SgitTransportCallback;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import android.content.ContentValues;

public class PullTask extends RepoOpTask implements OnPasswordEntered {

    private AsyncTaskCallback mCallback;

    public PullTask(Repo repo, AsyncTaskCallback callback) {
        super(repo);
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = pullRepo();
        if (mCallback != null) {
            result = mCallback.doInBackground(params) & result;
        }
        if (!result) {
            return false;
        }
        return true;
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
        Git git = mRepo.getGit();
        PullCommand pullCommand = git.pull()
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback());
        String username = mRepo.getUsername();
        String password = mRepo.getPassword();
        if (username != null && password != null && !username.equals("")
                && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    username, password);
            pullCommand.setCredentialsProvider(auth);
        }
        try {
            pullCommand.call();
        } catch (TransportException e) {
            setException(e);
            handleAuthError(this);
            return false;
        } catch (Exception e) {
            setException(e);
            setErrorRes(R.string.error_pull_failed);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e);
            setErrorRes(R.string.error_out_of_memory);
            return false;
        }
        return true;
    }

    @Override
    public void onClicked(String username, String password, boolean savePassword) {
        mRepo.setUsername(username);
        mRepo.setPassword(password);
        if (savePassword) {
            ContentValues values = new ContentValues();
            values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, username);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, password);
            RepoDbManager.updateRepo(mRepo.getID(), values);
        }

        mRepo.removeTask();
        PullTask pullTask = new PullTask(mRepo, mCallback);
        pullTask.executeTask();
    }

    @Override
    public void onCanceled() {
    }
}
