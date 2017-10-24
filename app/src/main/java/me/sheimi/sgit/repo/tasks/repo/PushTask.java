package me.sheimi.sgit.repo.tasks.repo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.util.Collection;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;
import me.sheimi.sgit.ssh.SgitTransportCallback;

public class PushTask extends RepoRemoteOpTask {

    private AsyncTaskCallback mCallback;
    private boolean mPushAll;
    private boolean mForcePush;
    private String mRemote;
    private StringBuffer resultMsg = new StringBuffer();

    public PushTask(Repo repo, String remote, boolean pushAll, boolean forcePush,
            AsyncTaskCallback callback) {
        super(repo);
        mRemote = remote;
        mCallback = callback;
        mPushAll = pushAll;
        mForcePush = forcePush;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = pushRepo();
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
        if (isSuccess) {
            BasicFunctions.getActiveActivity().showMessageDialog(
                    R.string.dialog_push_result, resultMsg.toString());
        }
    }

    public boolean pushRepo() {
        Git git;
        try {
            git = mRepo.getGit();
        } catch (StopTaskException e1) {
            return false;
        }
        PushCommand pushCommand = git.push().setPushTags()
                .setProgressMonitor(new BasicProgressMonitor())
                .setTransportConfigCallback(new SgitTransportCallback())
                .setRemote(mRemote);
        if (mPushAll) {
            pushCommand.setPushAll();
        } else {
            RefSpec spec = new RefSpec(mRepo.getBranchName());
            pushCommand.setRefSpecs(spec);
        }

        if (mForcePush) {
          pushCommand.setForce(true);
        }

        setCredentials(pushCommand);

        try {
            Iterable<PushResult> result = pushCommand.call();
            for (PushResult r : result) {
                Collection<RemoteRefUpdate> updates = r.getRemoteUpdates();
                for (RemoteRefUpdate update : updates) {
                    parseRemoteRefUpdate(update);
                }
            }
        } catch (TransportException e) {
            setException(e);
            handleAuthError(this);
            return false;
        } catch (Exception e) {
            setException(e);
            return false;
        } catch (OutOfMemoryError e) {
            setException(e, R.string.error_out_of_memory);
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

    private void parseRemoteRefUpdate(RemoteRefUpdate update) {
        String msg = null;
        switch (update.getStatus()) {
            case AWAITING_REPORT:
                msg = String
                        .format("[%s] Push process is awaiting update report from remote repository.\n",
                                update.getRemoteName());
                break;
            case NON_EXISTING:
                msg = String.format("[%s] Remote ref didn't exist.\n",
                        update.getRemoteName());
                break;
            case NOT_ATTEMPTED:
                msg = String
                        .format("[%s] Push process hasn't yet attempted to update this ref.\n",
                                update.getRemoteName());
                break;
            case OK:
                msg = String.format("[%s] Success push to remote ref.\n",
                        update.getRemoteName());
                break;
            case REJECTED_NODELETE:
                msg = String
                        .format("[%s] Remote ref update was rejected,"
                                + " because remote side doesn't support/allow deleting refs.\n",
                                update.getRemoteName());
                break;
            case REJECTED_NONFASTFORWARD:
                msg = String.format("[%s] Remote ref update was rejected,"
                        + " as it would cause non fast-forward update.\n",
                        update.getRemoteName());
            case REJECTED_OTHER_REASON:
                String reason = update.getMessage();
                if (reason == null || reason.isEmpty()) {
                    msg = String.format(
                            "[%s] Remote ref update was rejected.\n",
                            update.getRemoteName());
                } else {
                    msg = String
                            .format("[%s] Remote ref update was rejected, because %s.\n",
                                    update.getRemoteName(), reason);
                }
                break;
            case REJECTED_REMOTE_CHANGED:
                msg = String
                        .format("[%s] Remote ref update was rejected,"
                                + " because old object id on remote "
                                + "repository wasn't the same as defined expected old object.\n",
                                update.getRemoteName());
                break;
            case UP_TO_DATE:
                msg = String.format("[%s] remote ref is up to date\n",
                        update.getRemoteName());
                break;
        }
        resultMsg.append(msg);
    }

    @Override
    public RepoRemoteOpTask getNewTask() {
        return new PushTask(mRepo, mRemote, mPushAll, mForcePush, mCallback);
    }

}
