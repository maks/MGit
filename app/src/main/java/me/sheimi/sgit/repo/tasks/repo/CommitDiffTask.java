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
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;

public class CommitDiffTask extends RepoOpTask {

    private String mOldCommit;
    private String mNewCommit;
    private List<DiffEntry> mDiffEntries;
    private List<String> mDiffStrs;
    private CommitDiffResult mCallback;
    private boolean mShowDescription;
    private Iterable<RevCommit> mCommits;
    private DiffFormatter mDiffFormatter;
    private ByteArrayOutputStream mDiffOutput;

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

    private AbstractTreeIterator getTreeIterator(Repository repo, String commit) throws IOException {
        if (commit.equals("dircache")) {
            return new DirCacheIterator(repo.readDirCache());
        }
        if (commit.equals("filetree")) {
            return new FileTreeIterator(repo);
        }
        ObjectId treeId = repo.resolve(commit + "^{tree}");

        if (treeId == null) {
            throw new NullPointerException();
        }

        CanonicalTreeParser treeIter = new CanonicalTreeParser();
        ObjectReader reader = repo.newObjectReader();

        treeIter.reset(reader, treeId);
        return treeIter;
    }

    public boolean getCommitDiff() {
        try {
            Repository repo = mRepo.getGit().getRepository();

            mDiffOutput = new ByteArrayOutputStream();
            mDiffFormatter = new DiffFormatter(mDiffOutput);
            mDiffFormatter.setRepository(repo);

            AbstractTreeIterator mOldCommitTreeIterator = mRepo.isInitialCommit(mNewCommit) ?
                    new EmptyTreeIterator() : getTreeIterator(repo, mOldCommit);

            AbstractTreeIterator mNewCommitTreeIterator = getTreeIterator(repo, mNewCommit);
            mDiffEntries = mDiffFormatter.scan(mOldCommitTreeIterator, mNewCommitTreeIterator);

            if (mShowDescription) {
                ObjectId newCommitId = repo.resolve(mNewCommit);
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
        } catch (NullPointerException e) {
            setException(e, R.string.error_diff_failed);
        } catch (StopTaskException e) {
        }
        return false;
    }

    private String parseDiffEntry(DiffEntry diffEntry) throws StopTaskException {
        try {
            mDiffOutput.reset();
            mDiffFormatter.format(diffEntry);
            mDiffFormatter.flush();
            String diffText = mDiffOutput.toString("UTF-8");
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
