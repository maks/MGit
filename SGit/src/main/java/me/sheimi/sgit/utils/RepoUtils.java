package me.sheimi.sgit.utils;

import android.content.Context;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

/**
 * Created by sheimi on 8/16/13.
 */
public class RepoUtils {

    public static final String TEST_REPO = "https://github.com/sheimi/yurss" +
            ".git";

    private static RepoUtils mInstance;

    private Context mContext;

    private RepoUtils(Context context) {
        mContext = context;
    }

    public static RepoUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RepoUtils(context);
        }
        return mInstance;
    }

    public void cloneSync(String fromUri, String localRepoName) {
        File localRepo = new File(FsUtils.getInstance(mContext).getDir
                (FsUtils.REPO_DIR), localRepoName);
        try {
            Git.cloneRepository()
                    .setURI(fromUri)
                    .setDirectory(localRepo).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

}
