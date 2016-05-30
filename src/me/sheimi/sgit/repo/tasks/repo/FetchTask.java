package me.sheimi.sgit.repo.tasks.repo;

import android.content.ContentValues;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;
import me.sheimi.sgit.ssh.SgitTransportCallback;

public class FetchTask extends RepoOpTask implements SheimiFragmentActivity.OnPasswordEntered {

    private AsyncTaskCallback mCallback;

    public FetchTask(Repo repo, AsyncTaskCallback callback) {
        super(repo);
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = fetchRepo();
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

        mRepo.removeTask(this);
        FetchTask fetchTask = new FetchTask(mRepo, mCallback);
        fetchTask.executeTask();
    }

    @Override
    public void onCanceled() {}

    private boolean fetchRepo() {
        Git git;
        try {
            git = mRepo.getGit();
        } catch (StopTaskException e) {
            return false;
        }

        final FetchCommand fetchCommand = git.fetch()
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback());

        String username = mRepo.getUsername();
        String password = mRepo.getPassword();
        if (username != null && password != null && !username.equals("")
                && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    username, password);
            fetchCommand.setCredentialsProvider(auth);
        }

        try {
            fetchCommand.call();
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
}
