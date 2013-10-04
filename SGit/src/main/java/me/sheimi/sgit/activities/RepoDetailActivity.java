package me.sheimi.sgit.activities;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.umeng.analytics.MobclickAgent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.CommitDialog;
import me.sheimi.sgit.dialogs.DeleteRepoDialog;
import me.sheimi.sgit.dialogs.MergeDialog;
import me.sheimi.sgit.dialogs.PushRepoDialog;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

public class RepoDetailActivity extends SherlockFragmentActivity implements ActionBar.TabListener{

    private static final int[] NAV_TABS = {R.string.tab_files_label,
            R.string.tab_commits_label};
    private static final int DEFAULT_VALUE = -1;

    private static final int FILES_FRAGMENT_INDEX = 0;
    private static final int COMMITS_FRAGMENT_INDEX = 1;

    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private TabItemPagerAdapter mViewPagerAdapter;

    private FilesFragment mFilesFragment;
    private CommitsFragment mCommitsFragment;

    private RepoDbManager mDb;
    private RepoUtils mRepoUtils;
    private ViewUtils mViewUtils;
    private long mRepoID;
    private String mLocalPath;
    private String mUsername;
    private String mPassword;
    private Git mGit;
    private Thread mRunningThread;

    private View mPullProgressContainer;
    private ProgressBar mPullProgressBar;
    private TextView mPullMsg;
    private TextView mPullLeftHint;
    private TextView mPullRightHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        setupRepoDb();
        createFragments();
        setupPullProgressView();
        mViewUtils = ViewUtils.getInstance(this);
    }

    private void setupPullProgressView() {
        mPullProgressContainer = findViewById(R.id.pullProgressContainer);
        mPullProgressContainer.setVisibility(View.GONE);
        mPullProgressBar = (ProgressBar) mPullProgressContainer.findViewById(R.id.pullProgress);
        mPullMsg = (TextView) mPullProgressContainer.findViewById(R.id.pullMsg);
        mPullLeftHint = (TextView) mPullProgressContainer.findViewById(R.id.leftHint);
        mPullRightHint = (TextView) mPullProgressContainer.findViewById(R.id.rightHint);
    }

    private void setupActionBar() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mActionBar = getSupportActionBar();
        mViewPagerAdapter = new TabItemPagerAdapter
                (getSupportFragmentManager());

        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager
                .SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayShowTitleEnabled(true);

        for (int textId : NAV_TABS) {
            ActionBar.Tab tab = mActionBar.newTab()
                    .setText(getString(textId))
                    .setTabListener(this);
            mActionBar.addTab(tab);
        }

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupRepoDb() {
        mDb = RepoDbManager.getInstance(this);
        mRepoID = getIntent().getIntExtra(RepoContract.RepoEntry._ID,
                DEFAULT_VALUE);
        if (mRepoID == DEFAULT_VALUE)
            return;
        Cursor cursor = mDb.getRepoById(mRepoID);
        cursor.moveToFirst();
        Repo repo = new Repo(cursor);
        cursor.close();
        mLocalPath = repo.getLocalPath();
        mUsername = repo.getUsername();
        mPassword = repo.getPassword();
        setTitle(mLocalPath);
        mRepoUtils = RepoUtils.getInstance(this);
        mGit = mRepoUtils.getGit(mLocalPath);
    }

    private void createFragments() {
        mFilesFragment = FilesFragment.newInstance(mLocalPath);
        mCommitsFragment = CommitsFragment.newInstance(mLocalPath);
    }

    public void resetCommits(final String commitName) {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.checkout(mGit, commitName);
                mRepoUtils.updateLatestCommitInfo(mGit, mRepoID);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFilesFragment.reset(commitName);
                        mCommitsFragment.reset(commitName);
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.repo_detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtils.finishActivity(this);
                return true;
            case R.id.action_delete:
                DeleteRepoDialog drd = new DeleteRepoDialog(mRepoID, mLocalPath);
                drd.show(getSupportFragmentManager(), "delete-repo-dialog");
                return true;
            case R.id.action_pull:
                pullRepo();
                return true;
            case R.id.action_diff:
                mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
                mCommitsFragment.enterDiffActionMode();
                return true;
            case R.id.action_merge:
                MergeDialog md = new MergeDialog(mGit);
                md.show(getSupportFragmentManager(), "merge-repo-dialog");
                return true;
            case R.id.action_push:
                PushRepoDialog prd = new PushRepoDialog();
                prd.show(getSupportFragmentManager(), "push-repo-dialog");
                return true;
            case R.id.action_commit:
                CommitDialog cd = new CommitDialog();
                cd.show(getSupportFragmentManager(), "commit-dialog");
                return true;
            case R.id.action_reset:
                mViewUtils.showMessageDialog(R.string.dialog_reset_commit_title,
                        R.string.dialog_reset_commit_msg, R.string.action_reset,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetCommitChanges();
                    }
                });
                return true;
            case R.id.action_new_dir:
                mViewUtils.showEditTextDialog(R.string.dialog_create_dir_title,
                        R.string.dialog_create_dir_hint, R.string.label_create,
                        new ViewUtils.OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                mFilesFragment.newDir(text);
                                reset();
                            }
                        });
                return true;
            case R.id.action_new_file:
                mViewUtils.showEditTextDialog(R.string.dialog_create_file_title,
                        R.string.dialog_create_file_hint, R.string.label_create,
                        new ViewUtils.OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                mFilesFragment.newFile(text);
                                reset();
                            }
                        });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void pushRepo(final boolean pushAll) {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        Animation anim = AnimationUtils.loadAnimation(RepoDetailActivity.this,
                R.anim.fade_in);
        mPullProgressContainer.setAnimation(anim);
        mPullProgressContainer.setVisibility(View.VISIBLE);
        mPullMsg.setText(R.string.push_msg_init);
        mPullLeftHint.setText(R.string.push_left_init);
        mPullRightHint.setText(R.string.push_right_init);

        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.pushSync(mGit, mUsername, mPassword, getProgressMonitor(), pushAll);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Animation anim = AnimationUtils.loadAnimation(RepoDetailActivity.this,
                                R.anim.fade_out);
                        mPullProgressContainer.setAnimation(anim);
                        mPullProgressContainer.setVisibility(View.GONE);
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    private void pullRepo() {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        Animation anim = AnimationUtils.loadAnimation(RepoDetailActivity.this,
                R.anim.fade_in);
        mPullProgressContainer.setAnimation(anim);
        mPullProgressContainer.setVisibility(View.VISIBLE);
        mPullMsg.setText(R.string.pull_msg_init);
        mPullLeftHint.setText(R.string.pull_left_init);
        mPullRightHint.setText(R.string.pull_right_init);
        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.pullSync(mGit, mUsername, mPassword, getProgressMonitor());
                mRepoUtils.updateLatestCommitInfo(mGit, mRepoID);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Animation anim = AnimationUtils.loadAnimation(RepoDetailActivity.this,
                                R.anim.fade_out);
                        mPullProgressContainer.setAnimation(anim);
                        mPullProgressContainer.setVisibility(View.GONE);
                        reset();
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    private void reset() {
        mFilesFragment.reset();
        mCommitsFragment.reset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void setFilesFragment(FilesFragment filesFragment) {
        mFilesFragment = filesFragment;
    }

    public void setCommitsFragment(CommitsFragment commitsFragment) {
        mCommitsFragment = commitsFragment;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction
            fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    private class TabItemPagerAdapter extends
            FragmentStatePagerAdapter {

        public TabItemPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFragment getItem(int i) {
            switch (i) {
                case COMMITS_FRAGMENT_INDEX:
                    return mCommitsFragment;
                case FILES_FRAGMENT_INDEX:
                default:
                    return mFilesFragment;
            }
        }

        @Override
        public int getCount() { return NAV_TABS.length; }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(NAV_TABS[position]);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int position = mViewPager.getCurrentItem();
            OnBackClickListener onBackClickListener = mViewPagerAdapter
                    .getItem(position)
                    .getOnBackClickListener();
            if (onBackClickListener != null) {
                if (onBackClickListener.onClick())
                    return true;
            }
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

    public void mergeBranch(final Git git, final Ref commit, final String ffModeStr) {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.mergeBranch(git, commit, ffModeStr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    public void commitChanges(final String commitMsg) {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.commitAllChanges(mGit, commitMsg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        mViewUtils.showToastMessage(R.string.toast_commit_success);
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    private void resetCommitChanges() {
        if (mRunningThread != null) {
            mViewUtils.showToastMessage(R.string.alert_please_wait_previous_op);
            return;
        }
        mRunningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.resetCommitChanges(mGit);;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                        mViewUtils.showToastMessage(R.string.toast_reset_success);
                        mRunningThread = null;
                    }
                });
            }
        });
        mRunningThread.start();
    }

    private ProgressMonitor getProgressMonitor() {
        return new ProgressMonitor() {

            private int mTotalWork;
            private int mWorkDone;

            @Override
            public void start(int i) {
                Log.d("pull start", String.valueOf(i));
            }

            @Override
            public void beginTask(String title, int totalWork) {
                mTotalWork = totalWork;
                mWorkDone = 0;
                Log.d("pull beginTask", String.valueOf(totalWork));
                setProgress(title, mWorkDone, mTotalWork);
            }

            @Override
            public void update(int i) {
                mWorkDone += i;
                Log.d("pull update workDone", String.valueOf(mWorkDone));
                Log.d("pull update totlaWork", String.valueOf(mTotalWork));
                if (mTotalWork != ProgressMonitor.UNKNOWN && mTotalWork != 0) {
                    setProgress(null, mWorkDone, mTotalWork);
                }
            }

            @Override
            public void endTask() {

            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            private void setProgress(final String title, final int workDone, final int totalWork) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (title != null)
                            mPullMsg.setText(title + " ... ");
                        if (totalWork != 0) {
                            int showedWorkDown = Math.min(workDone, totalWork);
                            int progress = 100 * showedWorkDown / totalWork;
                            String leftHint = progress + "%";
                            String rightHint = showedWorkDown + "/" + totalWork;
                            mPullLeftHint.setText(leftHint);
                            mPullRightHint.setText(rightHint);
                            mPullProgressBar.setProgress(progress);
                            Log.d("pull update ui", String.valueOf(leftHint));
                            Log.d("pull update ui", String.valueOf(rightHint));
                        }
                    }
                });
            }

        };
    }
}
