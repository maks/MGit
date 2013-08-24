package me.sheimi.sgit.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.DeleteRepoDialog;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.RepoUtils;

public class RepoDetailActivity extends FragmentActivity implements ActionBar
        .TabListener {

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
    private long mRepoID;
    private String mLocalPath;
    private String mUsername;
    private String mPassword;
    private Repository mRepository;
    private Git mGit;

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
        mActionBar = getActionBar();
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
        mRepository = mRepoUtils.getRepository(mLocalPath);
        mGit = new Git(mRepository);
    }

    private void createFragments() {
        mFilesFragment = FilesFragment.newInstance(mLocalPath);
        mCommitsFragment = CommitsFragment.newInstance(mLocalPath);
    }

    public void resetCommits(final String commitName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.checkout(mGit, commitName);
                mRepoUtils.updateLatestCommitInfo(mGit, mRepoID);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFilesFragment.reset(commitName);
                        mCommitsFragment.reset(commitName);
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.repo_detail, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void pullRepo() {
        Animation anim = AnimationUtils.loadAnimation(RepoDetailActivity.this,
                R.anim.fade_in);
        mPullProgressContainer.setAnimation(anim);
        mPullProgressContainer.setVisibility(View.VISIBLE);
        mPullMsg.setText(R.string.pull_msg_init);
        mPullLeftHint.setText(R.string.pull_left_init);
        mPullRightHint.setText(R.string.pull_right_init);
        Thread thread = new Thread(new Runnable() {
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
                    }
                });
            }
        });
        thread.start(); ;
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

    private ProgressMonitor getProgressMonitor() {
        ProgressMonitor pm = new ProgressMonitor() {

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
                            int progress = 100 * workDone / totalWork;
                            progress = progress > 100 ? 100 : progress;
                            String leftHint = progress + "%";
                            String rightHint = workDone + "/" + totalWork;
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
        return pm;
    }
}
