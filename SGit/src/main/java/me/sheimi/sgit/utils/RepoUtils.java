package me.sheimi.sgit.utils;

import android.content.Context;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by sheimi on 8/16/13.
 */
public class RepoUtils {

    public static final String TEST_REPO = "https://github.com/sheimi/yurss" +
            ".git";
    public static final String GIT_DIR = "/.git";

    private static RepoUtils mInstance;

    private Context mContext;
    private FsUtils mFsUtils;

    private RepoUtils(Context context) {
        mContext = context;
        mFsUtils = FsUtils.getInstance(context);
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

    public void checkout(Git git, String name) {
        try {
            git.checkout().setName(name).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

}
