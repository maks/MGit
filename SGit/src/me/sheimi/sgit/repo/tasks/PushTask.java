package me.sheimi.sgit.repo.tasks;

import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.ssh.SgitTransportCallback;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import android.content.ContentValues;

public class PushTask extends RepoOpTask implements OnPasswordEntered {

    private AsyncTaskCallback mCallback;
    private boolean mPushAll;

    public PushTask(Repo repo, boolean pushAll, AsyncTaskCallback callback) {
        super(repo);
        mCallback = callback;
        mPushAll = pushAll;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = pushRepo();
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

    public boolean pushRepo() {
        Git git = mRepo.getGit();
        PushCommand pushCommand = git.push().setPushTags()
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback());
        if (mPushAll) {
            pushCommand.setPushAll();
        } else {
            RefSpec spec = new RefSpec(mRepo.getBranchName());
            pushCommand.setRefSpecs(spec);
        }

        String username = mRepo.getUsername();
        String password = mRepo.getPassword();
        if (username != null && password != null && !username.equals("")
                && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    username, password);
            pushCommand.setCredentialsProvider(auth);
        }

        try {
            pushCommand.call();
        } catch (TransportException e) {
            setException(e);
            handleAuthError(this);
            return false;
        } catch (Exception e) {
            setException(e);
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
        PushTask pushTask = new PushTask(mRepo, mPushAll, mCallback);
        pushTask.executeTask();
    }

    @Override
    public void onCanceled() {
    }

}
