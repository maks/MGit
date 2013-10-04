package me.sheimi.sgit.utils;

import android.content.ContentValues;
import android.content.Context;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
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
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    public static final int COMMIT_TYPE_REMOTE = 3;

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
        if (context != null) {
            mInstance.mContext = context;
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
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            mFsUtils.deleteFile(localRepo);
            mViewUtils.showToastMessage(R.string.error_out_of_memory);
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
        if (COMMIT_TYPE_REMOTE == getCommitType(name)) {
            checkoutFromRemote(git, name, getCommitName(name));
            return;
        }
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

    public void mergeBranch(Git git, Ref commit, String ffModeStr) {
        String[] stringArray = mContext.getResources().getStringArray(R.array.merge_ff_type);
        MergeCommand.FastForwardMode ffMode = MergeCommand.FastForwardMode.FF;
        if (ffModeStr.equals(stringArray[1])) {
            // FF Only
            ffMode = MergeCommand.FastForwardMode.FF_ONLY;
        } else if (ffModeStr.equals(stringArray[2])) {
            // No FF
            ffMode = MergeCommand.FastForwardMode.NO_FF;
        }
        try {
            git.merge().include(commit).setFastForward(ffMode).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        }
    }

    public void pushSync(Git git, String username, String password, ProgressMonitor pm,
                         boolean isPushAll) {
        PushCommand pushCommand = git.push().setPushTags().setProgressMonitor(pm)
                .setTransportConfigCallback(mSgitTransportCallback);
        if (isPushAll) {
            pushCommand.setPushAll();
        } else {
            RefSpec spec = new RefSpec(getBranchName(git));
            pushCommand.setRefSpecs(spec);
        }

        if (username != null && password != null && !username.equals("") && !password.equals("")) {
            UsernamePasswordCredentialsProvider auth =
                    new UsernamePasswordCredentialsProvider(username, password);
            pushCommand.setCredentialsProvider(auth);
        }

        try {
            pushCommand.call();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(R.string.error_invalid_remote);
        } catch (TransportException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        } catch (JGitInternalException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        }
    }

    public void commitAllChanges(Git git, String commitMsg) {
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(commitMsg).setAll(true).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
        }
    }

    public void resetCommitChanges(Git git) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
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

    public List<Ref> getLocalBranches(Git git) {
        try {
            List<Ref> localRefs = git.branchList().call();
            return localRefs;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getBranches(Git git) {
        try {
            Set<String> branchSet = new HashSet<String>();
            List<String> branchList = new ArrayList<String>();
            List<Ref> localRefs = git.branchList().call();
            for (Ref ref : localRefs) {
                branchSet.add(ref.getName());
                branchList.add(ref.getName());
            }
            List<Ref> remoteRefs = git.branchList().setListMode(ListBranchCommand.ListMode
                    .REMOTE).call();
            for (Ref ref : remoteRefs) {
                String name = ref.getName();
                String localName = convertRemoteName(name);
                if (branchSet.contains(localName))
                    continue;
                branchList.add(name);
            }
            return branchList.toArray(new String[0]);
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

    public String getBranchName(Git git) {
        try {
            return git.getRepository().getFullBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCommitDisplayName(String raw) {
        String[] splits = raw.split("/");
        int type = getCommitType(splits);
        switch (type) {
            case COMMIT_TYPE_TEMP:
                if (raw.length() <= 10)
                    return raw;
                return raw.substring(0, 10);
            case COMMIT_TYPE_TAG:
            case COMMIT_TYPE_HEAD:
                return splits[2];
            case COMMIT_TYPE_REMOTE:
                return splits[1] + "/" + splits[2] + "/" + splits[3];
        }
        return null;
    }

    public int getCommitType(String[] splits) {
        if (splits.length == 4)
            return COMMIT_TYPE_REMOTE;
        if (splits.length != 3)
            return COMMIT_TYPE_TEMP;
        String type = splits[1];
        if ("tags".equals(type))
            return COMMIT_TYPE_TAG;
        return COMMIT_TYPE_HEAD;
    }

    public int getCommitType(String str) {
        String[] splits = str.split("/");
        return getCommitType(splits);
    }

    public String convertRemoteName(String remote) {
        String[] splits = remote.split("/");
        if (getCommitType(splits) != COMMIT_TYPE_REMOTE)
            return null;
        return String.format("refs/heads/%s", splits[3]);
    }

    public String getCommitName(String name) {
        String[] splits = name.split("/");
        int type = getCommitType(splits);
        switch (type) {
            case COMMIT_TYPE_TEMP:
            case COMMIT_TYPE_TAG:
            case COMMIT_TYPE_HEAD:
                return getCommitDisplayName(name);
            case COMMIT_TYPE_REMOTE:
                return splits[3];
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
            if (msg != null) {
                values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMIT_MSG, msg);
            }
            if (email != null) {
                values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_EMAIL,
                        email);
            }
            if (uname != null) {
                values.put(RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_UNAME, uname);
            }
            RepoDbManager.getInstance(mContext).updateRepo(id,
                    values);
        }
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
