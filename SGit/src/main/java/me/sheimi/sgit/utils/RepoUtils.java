package me.sheimi.sgit.utils;

import android.content.ContentValues;
import android.content.Context;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.ssh.SgitTransportCallback;

/**
 * Created by sheimi on 8/16/13.
 */
public class RepoUtils {

    public static final String TEST_REPO = "git@git.sheimi.me:sheimi/sgit-android.git";
    public static final String TEST_LOCAL = "test";
    public static final String TEST_USERNAME = ""; // "sheimi.zhang@gmail.com";
    public static final String TEST_PASSWORD = ""; // "ZhangRizhen0923";
    public static final String DOT_GIT_DIR = "/.git";

    public static final int COMMIT_TYPE_HEAD = 0;
    public static final int COMMIT_TYPE_TAG = 1;
    public static final int COMMIT_TYPE_TEMP = 2;

    private static RepoUtils mInstance;

    private Context mContext;
    private FsUtils mFsUtils;
    private ViewUtils mViewUtils;
    private SgitTransportCallback mSgitTransportCallback;

    private RepoUtils(Context context) {
        mContext = context;
        mFsUtils = FsUtils.getInstance(mContext);
        mViewUtils = ViewUtils.getInstance(mContext);
        refreshSgitTransportCallback();
    }

    public static RepoUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RepoUtils(context);
        }
        return mInstance;
    }

    public void cloneSync(String fromUri, String localRepoName, String username,
                          String password, ProgressMonitor pm) throws GitAPIException {

        File localRepo = new File(mFsUtils.getDir(FsUtils.REPO_DIR),
                localRepoName);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(fromUri)
                .setCloneAllBranches(true)
                .setProgressMonitor(pm)
                .setTransportConfigCallback(mSgitTransportCallback)
                .setDirectory(localRepo);
        if (username != null && password != null && !username.equals("") && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth =
                    new UsernamePasswordCredentialsProvider(username, password);
            cloneCommand.setCredentialsProvider(auth);
        }
        try {
            cloneCommand.call();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
            mFsUtils.deleteFile(localRepo);
            mViewUtils.showToastMessage(R.string.error_invalid_remote);
            throw e;
        } catch (TransportException e) {
            e.printStackTrace();
            mFsUtils.deleteFile(localRepo);
            mViewUtils.showToastMessage(e.getMessage());
            throw e;
        } catch (GitAPIException e) {
            e.printStackTrace();
            mFsUtils.deleteFile(localRepo);
            mViewUtils.showToastMessage(R.string.error_clone_failed);
            throw e;
        } catch (JGitInternalException e) {
            e.printStackTrace();
            mFsUtils.deleteFile(localRepo);
            mViewUtils.showToastMessage(e.getMessage());
            throw e;
        }
    }

    public void pullSync(Git git, String username, String password, ProgressMonitor pm) {
        PullCommand pullCommand = git.pull().setProgressMonitor(pm)
                .setTransportConfigCallback(mSgitTransportCallback);
        if (username != null && password != null && !username.equals("") && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth =
                    new UsernamePasswordCredentialsProvider(username, password);
            pullCommand.setCredentialsProvider(auth);
        }
        try {
            pullCommand.call();
        } catch (TransportException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_pull_failed);
        }
    }

    public void checkout(Git git, String name) {
        try {
            git.checkout().setName(name).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void checkoutFromRemote(Git git, String remoteBranchName, String branchName) {
        try {
            git.checkout().setCreateBranch(true).setName(branchName)
                    .setStartPoint(remoteBranchName).call();
            git.branchCreate().setUpstreamMode(CreateBranchCommand.SetupUpstreamMode
                    .SET_UPSTREAM).setStartPoint(remoteBranchName).setName(branchName)
                    .setForce(true).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void checkoutAllGranches(Git git) {
        try {
            String oldBranchName = getBranchName(git);
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand
                    .ListMode.REMOTE).call();
            for (Ref ref : refs) {
                // ref/remotes/[remote]/[branch]
                String[] branchNameSplits = ref.getName().split("/");
                String remoteBranchName = branchNameSplits[2] + "/" +
                        branchNameSplits[3];
                String branchName = branchNameSplits[3];
                if (branchName.equals(oldBranchName))
                    continue;
                checkoutFromRemote(git, remoteBranchName, branchName);
            }
            checkout(git, oldBranchName);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public Repository getRepository(String localPath) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            File repoFile = new File(mFsUtils.getDir(FsUtils.REPO_DIR),
                    localPath + "/" + DOT_GIT_DIR);
            Repository repository = builder.setGitDir(repoFile)
                    .readEnvironment()
                    .findGitDir().build();
            return repository;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Repository gitRepoFromRepoPath(String repoPath) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            File repoFile = new File(repoPath);
            Repository repository = builder.setGitDir(repoFile)
                    .readEnvironment()
                    .findGitDir().build();
            return repository;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Git getGit(Repository repo) {
        Git git = new Git(repo);
        return git;
    }

    public Git getGit(String localPath) {
        File repoFile = new File(mFsUtils.getDir(FsUtils.REPO_DIR),
                localPath);
        try {
            return Git.open(repoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getBranches(Git git) {
        try {
            List<Ref> refs = git.branchList().call();
            String[] branches = new String[refs.size()];
            // convert refs/heads/[branch] -> heads/[branch]
            for (int i = 0; i < branches.length; i++) {
                branches[i] = refs.get(i).getName();
            }
            return branches;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getTags(Git git) {
        try {
            List<Ref> refs = git.tagList().call();
            String[] tags = new String[refs.size()];
            // convert refs/tags/[branch] -> heads/[branch]
            for (int i = 0; i < tags.length; i++) {
                tags[i] = refs.get(i).getName();
            }
            return tags;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCommitName(String str) {
        String[] splits = str.split("/");
        if (splits.length != 3)
            return null;
        // ref/[heads/]/name
        return splits[2];
    }

    public int getCommitType(String str) {
        String[] splits = str.split("/");
        if (splits.length != 3)
            return COMMIT_TYPE_TEMP;
        String type = splits[1];
        if ("tags".equals(type))
            return COMMIT_TYPE_TAG;
        return COMMIT_TYPE_HEAD;
    }

    public void updateLatestCommitInfo(Git git, long id) {
        RevCommit commit = getLatestCommit(git);
        ContentValues values = new ContentValues();
        if (commit != null) {
            PersonIdent committer = commit.getCommitterIdent();
            String email = committer.getEmailAddress();
            String uname = committer.getName();
            long date = committer.getWhen().getTime();
            String msg = commit.getShortMessage();

            values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMIT_DATE,
                    Long.toString(date));
            values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMIT_MSG, msg);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_EMAIL,
                    email);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_UNAME, uname);
        }
        RepoDbManager.getInstance(mContext).updateRepo(id,
                values);
    }

    public String getBranchName(Git git) {
        try {
            return git.getRepository().getFullBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getShortenCommitName(RevCommit commit) {
        return getShortenCommitName(commit.getName());
    }

    public String getShortenCommitName(String name) {
        if (name.length() <= 10)
            return name;
        return name.substring(0, 10);
    }

    public String getCommitDisplayName(String raw) {
        int type = getCommitType(raw);
        switch (type) {
            case COMMIT_TYPE_TEMP:
                return getShortenCommitName(raw);
            case COMMIT_TYPE_TAG:
            case COMMIT_TYPE_HEAD:
                return getCommitName(raw);
        }
        return null;
    }

    public List<RevCommit> getCommitsList(Git git) {
        try {
            Iterable<RevCommit> commits = git.log().call();
            List<RevCommit> commitsList = new ArrayList<RevCommit>();
            for (RevCommit commit : commits) {
                commitsList.add(commit);
            }
            return commitsList;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RevCommit getLatestCommit(Git git) {
        try {
            Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
            Iterator<RevCommit> it = commits.iterator();
            if (!it.hasNext())
                return null;
            return it.next();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void refreshSgitTransportCallback() {
        mSgitTransportCallback = new SgitTransportCallback(mContext);
    }

    public List<DiffEntry> getCommitDiff(Git git, String oldCommit, String newCommit) {
        Repository repo = git.getRepository();
        try {
            ObjectId oldId = repo.resolve(oldCommit + "^{tree}");
            ObjectId newId = repo.resolve(newCommit + "^{tree}");

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

            ObjectReader reader = repo.newObjectReader();

            oldTreeIter.reset(reader, oldId);
            newTreeIter.reset(reader, newId);

            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter).call();

            return diffs;
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_diff_failed);
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_diff_failed);
        } catch (AmbiguousObjectException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_diff_failed);
        } catch (IOException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_diff_failed);
        }
        return null;
    }

    public String parseDiffEntry(Git git, DiffEntry diffEntry) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(git.getRepository());

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
    
    public String getRemoteOriginURL(Git git) {
        StoredConfig config = git.getRepository().getConfig();
        String origin = config.getString("remote", "origin", "url");
        return origin;
    }

}
