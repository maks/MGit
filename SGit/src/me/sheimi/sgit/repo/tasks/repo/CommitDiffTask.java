package me.sheimi.sgit.repo.tasks.repo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class CommitDiffTask extends RepoOpTask {

    private String mOldCommit;
    private String mNewCommit;
    private List<DiffEntry> mDiffEntries;
    private List<String> mDiffStrs;
    private CommitDiffResult mCallback;

    public interface CommitDiffResult {
        public void pushResult(List<DiffEntry> diffEntries,
                List<String> diffStrs);
    }

    public CommitDiffTask(Repo repo, String oldCommit, String newCommit,
            CommitDiffResult callback) {
        super(repo);
        mOldCommit = oldCommit;
        mNewCommit = newCommit;
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = getCommitDiff();
        if (!result) {
            return false;
        }
        mDiffStrs = new ArrayList<String>(mDiffEntries.size());
        for (DiffEntry diffEntry : mDiffEntries) {
            String diffStr = parseDiffEntry(diffEntry);
            mDiffStrs.add(diffStr);
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (isSuccess && mCallback != null && mDiffEntries != null) {
            mCallback.pushResult(mDiffEntries, mDiffStrs);
        }
    }

    public boolean getCommitDiff() {
        Repository repo = mRepo.getGit().getRepository();
        try {
            ObjectId oldId = repo.resolve(mOldCommit + "^{tree}");
            ObjectId newId = repo.resolve(mNewCommit + "^{tree}");

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

            ObjectReader reader = repo.newObjectReader();

            oldTreeIter.reset(reader, oldId);
            newTreeIter.reset(reader, newId);

            mDiffEntries = mRepo.getGit().diff().setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter).call();

            return true;
        } catch (GitAPIException e) {
            setException(e);
        } catch (IncorrectObjectTypeException e) {
            setException(e);
            setErrorRes(R.string.error_diff_failed);
        } catch (AmbiguousObjectException e) {
            setException(e);
            setErrorRes(R.string.error_diff_failed);
        } catch (IOException e) {
            setException(e);
            setErrorRes(R.string.error_diff_failed);
        } catch (IllegalStateException e) {
            setException(e);
            setErrorRes(R.string.error_diff_failed);
        }
        return false;
    }

    private String parseDiffEntry(DiffEntry diffEntry) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(mRepo.getGit().getRepository());
        try {
            df.format(diffEntry);
            String diffText = out.toString("UTF-8");
            return diffText;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
