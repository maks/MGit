package me.sheimi.sgit.repo.tasks.repo;

import java.util.ArrayList;
import java.util.List;

import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GetCommitTask extends RepoOpTask {

    private GetCommitCallback mCallback;
    private List<RevCommit> mResult;

    public static interface GetCommitCallback {
        public void postCommits(List<RevCommit> commits);
    }

    public void executeTask() {
        execute();
    }

    public GetCommitTask(Repo repo, GetCommitCallback callback) {
        super(repo);
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = getCommitsList();
        if (!result) {
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.postCommits(mResult);
        }
    }

    public boolean getCommitsList() {
        try {
            Iterable<RevCommit> commits = mRepo.getGit().log().call();
            mResult = new ArrayList<RevCommit>();
            for (RevCommit commit : commits) {
                mResult.add(commit);
            }
        } catch (GitAPIException e) {
            setException(e);
            return false;
        }
        return true;
    }

}
