package me.sheimi.sgit.database.models;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.RepoCloneMonitor.CloneObserver;
import me.sheimi.sgit.dialogs.ProfileDialog;
import me.sheimi.sgit.ssh.SgitTransportCallback;

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
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

/**
 * Created by sheimi on 8/20/13.
 */
public class Repo implements Comparable<Repo>, Serializable {

    /**
     * Generated serialVersionID
     */
    private static final long serialVersionUID = -4921633809823078219L;

    public static final String TAG = "repo";

    public static final int COMMIT_TYPE_HEAD = 0;
    public static final int COMMIT_TYPE_TAG = 1;
    public static final int COMMIT_TYPE_TEMP = 2;
    public static final int COMMIT_TYPE_REMOTE = 3;

    private int mID;
    private String mLocalPath;
    private String mRemoteURL;
    private String mUsername;
    private String mPassword;
    private String mRepoStatus;
    private String mLastCommitter;
    private String mLastCommitterEmail;
    private Date mLastCommitDate;
    private String mLastCommitMsg;
    private RepoCloneMonitor mCloneMonitor;
    private boolean isDeleted = false;

    private Context mContext;
    private RepoDbManager mDbManager;
    private Git mGit;

    public static final String TEST_REPO = "https://github.com/sheimi/SGit.git";
    public static final String TEST_LOCAL = "SGit";
    public static final String DOT_GIT_DIR = ".git";

    public Repo() {

    }

    public Repo(Context context, Cursor cursor) {
        mID = RepoContract.getRepoID(cursor);
        mRemoteURL = RepoContract.getRemoteURL(cursor);
        mLocalPath = RepoContract.getLocalPath(cursor);
        mUsername = RepoContract.getUsername(cursor);
        mPassword = RepoContract.getPassword(cursor);
        mRepoStatus = RepoContract.getRepoStatus(cursor);
        mLastCommitter = RepoContract.getLatestCommitterName(cursor);
        mLastCommitterEmail = RepoContract.getLatestCommitterEmail(cursor);
        mLastCommitDate = RepoContract.getLatestCommitDate(cursor);
        mLastCommitMsg = RepoContract.getLatestCommitMsg(cursor);

        setContext(context);
    }

    public void setContext(Context context) {
        mContext = context;
        mDbManager = RepoDbManager.getInstance(context);
        mGit = getGit();
    }

    public static Repo getRepoById(Context context, long id) {
        Cursor c = RepoDbManager.getInstance(context).getRepoById(id);
        c.moveToFirst();
        Repo repo = new Repo(context, c);
        c.close();
        return repo;
    }

    public static List<Repo> getRepoList(Context context, Cursor cursor) {
        List<Repo> repos = new ArrayList<Repo>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            repos.add(new Repo(context, cursor));
            cursor.moveToNext();
        }
        return repos;
    }

    public int getID() {
        return mID;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public String getRemoteURL() {
        return mRemoteURL;
    }

    public String getRepoStatus() {
        return mRepoStatus;
    }

    public String getLastCommitter() {
        return mLastCommitter;
    }

    public String getLastCommitterEmail() {
        return mLastCommitterEmail;
    }

    public String getLastCommitMsg() {
        return mLastCommitMsg;
    }

    public Date getLastCommitDate() {
        return mLastCommitDate;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public int getProgress() {
        if (mCloneMonitor == null)
            return 0;
        return mCloneMonitor.getProgress();
    }

    public void cancelClone() {
        if (mCloneMonitor != null) {
            mCloneMonitor.cancel();
        }
        deleteRepo();
    }

    public void updateStatus(String status) {
        ContentValues values = new ContentValues();
        mRepoStatus = status;
        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS, status
                + " ...");
        mDbManager.updateRepo(mID, values);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(mID);
        out.writeObject(mRemoteURL);
        out.writeObject(mLocalPath);
        out.writeObject(mUsername);
        out.writeObject(mPassword);
        out.writeObject(mRepoStatus);
        out.writeObject(mLastCommitter);
        out.writeObject(mLastCommitterEmail);
        out.writeObject(mLastCommitDate);
        out.writeObject(mLastCommitMsg);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        mID = in.readInt();
        mRemoteURL = (String) in.readObject();
        mLocalPath = (String) in.readObject();
        mUsername = (String) in.readObject();
        mPassword = (String) in.readObject();
        mRepoStatus = (String) in.readObject();
        mLastCommitter = (String) in.readObject();
        mLastCommitterEmail = (String) in.readObject();
        mLastCommitDate = (Date) in.readObject();
        mLastCommitMsg = (String) in.readObject();
    }

    @Override
    public int compareTo(Repo repo) {
        return repo.getID() - getID();
    }

    public void deleteRepo() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                deleteRepoSync();
            }
        });
        thread.start();
    }

    public void deleteRepoSync() {
        if (isDeleted)
            return;
        mDbManager.deleteRepo(mID);
        File fileToDelete = FsUtils.getRepo(mLocalPath);
        FsUtils.deleteFile(fileToDelete);
        isDeleted = true;
    }

    public void resetCommitChanges() {
        try {
            mGit.reset().setMode(ResetCommand.ResetType.HARD).call();
        } catch (GitAPIException e) {
            showError(e);
        }
    }

    public void commitAllChanges(String commitMsg) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String committerName = sharedPreferences.getString(
                ProfileDialog.GIT_USER_NAME, "");
        String committerEmail = sharedPreferences.getString(
                ProfileDialog.GIT_USER_EMAIL, "");
        try {
            mGit.add().addFilepattern(".").call();
            mGit.commit().setMessage(commitMsg)
                    .setCommitter(committerName, committerEmail).setAll(true)
                    .call();
            updateLatestCommitInfo();
        } catch (GitAPIException e) {
            showError(e);
        }
    }

    public void checkout(String name) {
        if (COMMIT_TYPE_REMOTE == getCommitType(name)) {
            checkoutFromRemote(name, getCommitName(name));
            return;
        }
        try {
            mGit.checkout().setName(name).call();
        } catch (GitAPIException e) {
            showError(e);
        } catch (JGitInternalException e) {
            showError(e);
            throw e;
        }
    }

    private void checkoutFromRemote(String remoteBranchName, String branchName) {
        try {
            mGit.checkout().setCreateBranch(true).setName(branchName)
                    .setStartPoint(remoteBranchName).call();
            mGit.branchCreate()
                    .setUpstreamMode(
                            CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .setStartPoint(remoteBranchName).setName(branchName)
                    .setForce(true).call();
        } catch (GitAPIException e) {
            showError(e);
        } catch (JGitInternalException e) {
            showError(e);
            throw e;
        }
    }

    public String getBranchName() {
        try {
            return mGit.getRepository().getFullBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getBranches() {
        try {
            Set<String> branchSet = new HashSet<String>();
            List<String> branchList = new ArrayList<String>();
            List<Ref> localRefs = mGit.branchList().call();
            for (Ref ref : localRefs) {
                branchSet.add(ref.getName());
                branchList.add(ref.getName());
            }
            List<Ref> remoteRefs = mGit.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE).call();
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

    public void pull(ProgressMonitor pm) throws TransportException {
        PullCommand pullCommand = mGit
                .pull()
                .setProgressMonitor(pm)
                .setTransportConfigCallback(new SgitTransportCallback());
        if (mUsername != null && mPassword != null && !mUsername.equals("")
                && !mPassword.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    mUsername, mPassword);
            pullCommand.setCredentialsProvider(auth);
        }
        try {
            pullCommand.call();
        } catch (TransportException e) {
            showError(e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            showError(R.string.error_pull_failed);
        }
    }

    public void push(ProgressMonitor pm, boolean isPushAll)
            throws TransportException {
        PushCommand pushCommand = mGit
                .push()
                .setPushTags()
                .setProgressMonitor(pm)
                .setTransportConfigCallback(new SgitTransportCallback());
        if (isPushAll) {
            pushCommand.setPushAll();
        } else {
            RefSpec spec = new RefSpec(getBranchName());
            pushCommand.setRefSpecs(spec);
        }

        if (mUsername != null && mPassword != null && !mUsername.equals("")
                && !mPassword.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    mUsername, mPassword);
            pushCommand.setCredentialsProvider(auth);
        }

        try {
            pushCommand.call();
        } catch (TransportException e) {
            showError(e);
            throw e;
        } catch (Exception e) {
            showError(e);
        }
    }

    public void clone(CloneObserver observer) throws GitAPIException {
        mCloneMonitor = new RepoCloneMonitor(this, observer);
        File localRepo = new File(FsUtils.getDir(FsUtils.REPO_DIR), mLocalPath);
        CloneCommand cloneCommand = Git
                .cloneRepository()
                .setURI(mRemoteURL)
                .setCloneAllBranches(true)
                .setProgressMonitor(mCloneMonitor)
                .setTransportConfigCallback(new SgitTransportCallback())
                .setDirectory(localRepo);

        if (mUsername != null && mPassword != null && !mUsername.equals("")
                && !mPassword.equals("")) {
            UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider(
                    mUsername, mPassword);
            cloneCommand.setCredentialsProvider(auth);
        }
        try {
            cloneCommand.call();
            mGit = getGit();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
            showError(R.string.error_invalid_remote);
            throw e;
        } catch (TransportException e) {
            showError(e);
            throw e;
        } catch (GitAPIException e) {
            e.printStackTrace();
            showError(R.string.error_clone_failed);
            throw e;
        } catch (JGitInternalException e) {
            showError(e);
            throw e;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            showError(R.string.error_out_of_memory);
            throw e;
        } catch (RuntimeException e) {
            showError(e);
        }
    }

    public void updateLatestCommitInfo() {
        RevCommit commit = getLatestCommit();
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
                values.put(
                        RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMIT_MSG,
                        msg);
            }
            if (email != null) {
                values.put(
                        RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_EMAIL,
                        email);
            }
            if (uname != null) {
                values.put(
                        RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_UNAME,
                        uname);
            }
            mDbManager.updateRepo(getID(), values);
        }
    }

    public RevCommit getLatestCommit() {
        try {
            Iterable<RevCommit> commits = mGit.log().setMaxCount(1).call();
            Iterator<RevCommit> it = commits.iterator();
            if (!it.hasNext())
                return null;
            return it.next();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getCommitType(String[] splits) {
        if (splits.length == 4)
            return COMMIT_TYPE_REMOTE;
        if (splits.length != 3)
            return COMMIT_TYPE_TEMP;
        String type = splits[1];
        if ("tags".equals(type))
            return COMMIT_TYPE_TAG;
        return COMMIT_TYPE_HEAD;
    }

    public List<DiffEntry> getCommitDiff(String oldCommit, String newCommit) {
        Repository repo = mGit.getRepository();
        try {
            ObjectId oldId = repo.resolve(oldCommit + "^{tree}");
            ObjectId newId = repo.resolve(newCommit + "^{tree}");

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

            ObjectReader reader = repo.newObjectReader();

            oldTreeIter.reset(reader, oldId);
            newTreeIter.reset(reader, newId);

            List<DiffEntry> diffs = mGit.diff().setOldTree(oldTreeIter)
                    .setNewTree(newTreeIter).call();

            return diffs;
        } catch (GitAPIException e) {
            e.printStackTrace();
            showError(R.string.error_diff_failed);
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
            showError(R.string.error_diff_failed);
        } catch (AmbiguousObjectException e) {
            e.printStackTrace();
            showError(R.string.error_diff_failed);
        } catch (IOException e) {
            e.printStackTrace();
            showError(R.string.error_diff_failed);
        } catch (IllegalStateException e) {
            showError(e);
        }
        return null;
    }

    public String parseDiffEntry(DiffEntry diffEntry) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(mGit.getRepository());
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

    public List<Ref> getLocalBranches() {
        try {
            List<Ref> localRefs = mGit.branchList().call();
            return localRefs;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getTags() {
        try {
            List<Ref> refs = mGit.tagList().call();
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

    public List<RevCommit> getCommitsList() {
        try {
            Iterable<RevCommit> commits = mGit.log().call();
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

    public String getRemoteOriginURL() {
        StoredConfig config = mGit.getRepository().getConfig();
        String origin = config.getString("remote", "origin", "url");
        return origin;
    }

    public void mergeBranch(Ref commit, String ffModeStr, boolean autoCommit) {
        String[] stringArray = mContext.getResources().getStringArray(
                R.array.merge_ff_type);
        MergeCommand.FastForwardMode ffMode = MergeCommand.FastForwardMode.FF;
        if (ffModeStr.equals(stringArray[1])) {
            // FF Only
            ffMode = MergeCommand.FastForwardMode.FF_ONLY;
        } else if (ffModeStr.equals(stringArray[2])) {
            // No FF
            ffMode = MergeCommand.FastForwardMode.NO_FF;
        }
        try {
            mGit.merge().include(commit).setCommit(autoCommit)
                    .setFastForward(ffMode).call();
            updateLatestCommitInfo();
        } catch (GitAPIException e) {
            showError(e);
        }
    }

    public String getCurrentDisplayName() {
        return getCommitDisplayName(getBranchName());
    }

    public static int getCommitType(String str) {
        String[] splits = str.split("/");
        return getCommitType(splits);
    }

    public static String getCommitName(String name) {
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

    public static String getCommitDisplayName(String raw) {
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

    public static String convertRemoteName(String remote) {
        String[] splits = remote.split("/");
        if (getCommitType(splits) != COMMIT_TYPE_REMOTE)
            return null;
        return String.format("refs/heads/%s", splits[3]);
    }

    private Git getGit() {
        File repoFile = new File(FsUtils.getDir(FsUtils.REPO_DIR),
                getLocalPath());
        try {
            return Git.open(repoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showError(Exception e) {
        e.printStackTrace();
        if (mContext instanceof SheimiFragmentActivity) {
            ((SheimiFragmentActivity) mContext)
                    .showToastMessage(e.getMessage());
        }
    }

    private void showError(int errorId) {
        if (mContext instanceof SheimiFragmentActivity) {
            ((SheimiFragmentActivity) mContext).showToastMessage(errorId);
        }
    }

}
