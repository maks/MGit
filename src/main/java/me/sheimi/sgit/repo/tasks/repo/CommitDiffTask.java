package me.sheimi.sgit.repo.tasks.repo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class CommitDiffTask extends RepoOpTask {

    private String mOldCommit;
    private String mNewCommit;
    private List<DiffEntry> mDiffEntries;
    private List<String> mDiffStrs;
    private CommitDiffResult mCallback;
    private boolean mShowDescription;
    private Iterable<RevCommit> mCommits;

    public interface CommitDiffResult {
        public void pushResult(List<DiffEntry> diffEntries,
			       List<String> diffStrs, RevCommit description);
    }

    public CommitDiffTask(Repo repo, String oldCommit, String newCommit,
			  CommitDiffResult callback, boolean showDescription) {
        super(repo);
        mOldCommit = oldCommit;
        mNewCommit = newCommit;
        mCallback = callback;
	mShowDescription = showDescription;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = getCommitDiff();
        if (!result) {
            return false;
        }
        mDiffStrs = new ArrayList<String>(mDiffEntries.size());
        for (DiffEntry diffEntry : mDiffEntries) {
            try {
                String diffStr = parseDiffEntry(diffEntry);
                mDiffStrs.add(diffStr);
            } catch (StopTaskException e) {
                return false;
            }
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
	RevCommit retCommit = null;
        if (isSuccess && mCallback != null && mDiffEntries != null) {
	    if (mCommits != null) {
		for (RevCommit commit : mCommits) {
		    retCommit = commit;
		    break;
		}
	    }
            mCallback.pushResult(mDiffEntries, mDiffStrs, retCommit);
        }
    }

    public boolean getCommitDiff() {
        try {
            Repository repo = mRepo.getGit().getRepository();
            ObjectId oldId = repo.resolve(mOldCommit + "^{tree}");
            ObjectId newId = repo.resolve(mNewCommit + "^{tree}");
            ObjectId newCommitId = repo.resolve(mNewCommit);

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

            ObjectReader reader = repo.newObjectReader();

            oldTreeIter.reset(reader, oldId);
            newTreeIter.reset(reader, newId);

            mDiffEntries = mRepo.getGit().diff().setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter).call();
	    if (mShowDescription) {
		mCommits = mRepo.getGit().log().add(newCommitId).setMaxCount(1).call();
	    } else {
		mCommits = new ArrayList<RevCommit>();
	    }

            return true;
        } catch (GitAPIException e) {
            setException(e);
        } catch (IncorrectObjectTypeException e) {
            setException(e, R.string.error_diff_failed);
        } catch (AmbiguousObjectException e) {
            setException(e, R.string.error_diff_failed);
        } catch (IOException e) {
            setException(e, R.string.error_diff_failed);
        } catch (IllegalStateException e) {
            setException(e, R.string.error_diff_failed);
        } catch (StopTaskException e) {
        }
        return false;
    }

    private String parseDiffEntry(DiffEntry diffEntry) throws StopTaskException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        try {
            df.setRepository(mRepo.getGit().getRepository());
            df.format(diffEntry);
            String diffText = out.toString("UTF-8");
            return diffText;
        } catch (UnsupportedEncodingException e) {
            setException(e, R.string.error_diff_failed);
            throw new StopTaskException();
        } catch (IOException e) {
            setException(e, R.string.error_diff_failed);
            throw new StopTaskException();
        }
    }

    public void executeTask() {
        execute();
    }

}
