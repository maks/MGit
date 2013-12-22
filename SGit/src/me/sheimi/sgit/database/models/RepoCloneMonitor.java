package me.sheimi.sgit.database.models;

import org.eclipse.jgit.lib.ProgressMonitor;

import android.util.Log;

public class RepoCloneMonitor implements ProgressMonitor {

    private Repo mRepo;
    private int mTotalWork;
    private int mWorkDone;
    private int mProgress;
    private CloneObserver mObserver;
    private boolean mIsCanceled;
    
    public static interface CloneObserver {
        public void cloneStateUpdated();
    }

    public RepoCloneMonitor(Repo repo, CloneObserver observer) {
        mRepo = repo;
        mObserver = observer;
        mIsCanceled = false;
    }

    public int getProgress() {
        return mProgress;
    }
    
    public void cancel() {
        mIsCanceled = true;
    }
    
    @Override
    public void start(int totalTasks) {
        mProgress = 0;
    }

    @Override
    public void beginTask(String title, int totalWork) {
        mTotalWork = totalWork;
        mWorkDone = 0;
        mProgress = 0;
        if (title != null) {
            mRepo.updateStatus(title);
        }
    }

    @Override
    public void update(int i) {
        mWorkDone += i;
        if (mTotalWork != 0) {
            mProgress = computeProgress();
            mObserver.cloneStateUpdated();
        }
    }

    @Override
    public void endTask() {
    }

    @Override
    public boolean isCancelled() {
        Log.i(RepoCloneMonitor.class.getName(), Boolean.toString(mIsCanceled));
        return mIsCanceled;
    }

    private int computeProgress() {
        return 100 * mWorkDone / mTotalWork;
    }

}