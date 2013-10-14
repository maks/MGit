package me.sheimi.sgit.utils;

import android.content.Context;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

import me.sheimi.sgit.utils.ssh.SgitTransportCallback;

/**
 * Created by sheimi on 8/16/13.
 */
public class RepoUtils {

    public static final String TEST_REPO = "git@git.sheimi.me:sheimi/sgit-android.git";
    public static final String TEST_LOCAL = "test";
    public static final String TEST_USERNAME = "";
    public static final String TEST_PASSWORD = "";
    public static final String DOT_GIT_DIR = "/.git";

    public static final int COMMIT_TYPE_HEAD = 0;
    public static final int COMMIT_TYPE_TAG = 1;
    public static final int COMMIT_TYPE_TEMP = 2;
    public static final int COMMIT_TYPE_REMOTE = 3;

    private static RepoUtils mInstance;

    private Context mContext;
    private FsUtils mFsUtils;
    private SgitTransportCallback mSgitTransportCallback;

    private RepoUtils(Context context) {
        mContext = context;
        mFsUtils = FsUtils.getInstance(mContext);
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





    public void refreshSgitTransportCallback() {
        mSgitTransportCallback = new SgitTransportCallback(mContext);
    }

    public SgitTransportCallback getSgitTransportCallback() {
        return mSgitTransportCallback;
    }




}
