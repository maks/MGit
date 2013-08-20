package me.sheimi.sgit.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
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
    private int mRepoID;
    private String mLocalPath;
    private Repository mRepository;
    private Git mGit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        setupRepoDb();
        createFragments();
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
        mLocalPath = RepoContract.getLocalPath(cursor);
        setTitle(mLocalPath);
        mRepoUtils = RepoUtils.getInstance(this);
        mRepository = mRepoUtils.getRepository(mLocalPath);
        mGit = new Git(mRepository);
    }

    private void createFragments() {
        mFilesFragment = new FilesFragment(mGit, mLocalPath);
        mCommitsFragment = new CommitsFragment(mGit);
    }

    public void resetCommits(final String commitName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.checkout(mGit, commitName);
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
                NavUtils.navigateUpFromSameTask(this);
                ActivityUtils.backTransition(this);
                return true;
            case R.id.action_pull:
                pullRepo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pullRepo() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRepoUtils.pullSync(mGit, new ProgressMonitor() {
                    @Override
                    public void start(int i) {
                    }

                    @Override
                    public void beginTask(String s, int i) {

                    }

                    @Override
                    public void update(int i) {

                    }

                    @Override
                    public void endTask() {

                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                });
            }
        });
        thread.start();;
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
}
