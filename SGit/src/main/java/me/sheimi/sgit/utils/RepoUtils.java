package me.sheimi.sgit.utils;

import android.content.Context;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheimi on 8/16/13.
 */
public class RepoUtils {

    public static final String TEST_REPO =
            "https://github.com/sheimi/blog.sheimi.me.git";
    public static final String GIT_DIR = "/.git";

    public static final int COMMIT_TYPE_HEAD = 0;
    public static final int COMMIT_TYPE_TAG = 1;
    public static final int COMMIT_TYPE_TEMP = 2;

    private static final String REMOTE_TAG = "remotes";
    private static RepoUtils mInstance;

    private Context mContext;
    private FsUtils mFsUtils;

    private RepoUtils(Context context) {
        mContext = context;
        mFsUtils = FsUtils.getInstance(mContext);
    }

    public static RepoUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RepoUtils(context);
        }
        return mInstance;
    }

    public void cloneSync(String fromUri, String localRepoName) {
        File localRepo = new File(mFsUtils.getDir(FsUtils.REPO_DIR),
                localRepoName);
        try {
            Git.cloneRepository()
                    .setURI(fromUri)
                    .setCloneAllBranches(true)
                    .setDirectory(localRepo).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public Repository getRepository(String localPath) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            File repoFile = new File(mFsUtils.getDir(FsUtils.REPO_DIR),
                    localPath + GIT_DIR);
            Repository repository = builder.setGitDir(repoFile)
                    .readEnvironment()
                    .findGitDir().build();
            return repository;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public void checkout(Git git, String name) {
        try {
            git.checkout().setName(name).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public String getBranchName(Git git) {
        try {
            return git.getRepository().getFullBranch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void checkoutAllGranches(Git git) {
        try {
            String oldBranchName = getBranchName(git);
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand
                    .ListMode.ALL).call();
            for (Ref ref : refs) {
                String[] branchNameSplits = ref.getName().split("/");
                // if the second item is not "remotes"
                if (!REMOTE_TAG.equals(branchNameSplits[1]))
                    continue;
                String remoteBranchName = branchNameSplits[2] + "/" +
                        branchNameSplits[3];
                String branchName = branchNameSplits[3];
                git.checkout().setCreateBranch(true).setName(branchName)
                        .setUpstreamMode(CreateBranchCommand
                                .SetupUpstreamMode.TRACK)
                        .setStartPoint(remoteBranchName).call();
            }
            checkout(git, oldBranchName);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
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

}
