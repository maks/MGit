package me.sheimi.sgit.activities;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.MergeDialog;
import me.sheimi.sgit.dialogs.PushRepoDialog;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.repo.tasks.CheckoutTask;
import me.sheimi.sgit.repo.tasks.CommitChangesTask;
import me.sheimi.sgit.repo.tasks.MergeTask;
import me.sheimi.sgit.repo.tasks.PullTask;
import me.sheimi.sgit.repo.tasks.PushTask;
import me.sheimi.sgit.repo.tasks.ResetCommitTask;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskCallback;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;

import org.eclipse.jgit.lib.Ref;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RepoDetailActivity extends SheimiFragmentActivity implements
        ActionBar.TabListener {

    private static final int[] NAV_TABS = { R.string.tab_files_label,
            R.string.tab_commits_label };

    private static final int FILES_FRAGMENT_INDEX = 0;
    private static final int COMMITS_FRAGMENT_INDEX = 1;

    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private TabItemPagerAdapter mViewPagerAdapter;

    private FilesFragment mFilesFragment;
    private CommitsFragment mCommitsFragment;

    private Repo mRepo;

    private View mPullProgressContainer;
    private ProgressBar mPullProgressBar;
    private TextView mPullMsg;
    private TextView mPullLeftHint;
    private TextView mPullRightHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        setTitle(mRepo.getLocalPath());
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        createFragments();
        setupPullProgressView();
    }

    private void setupPullProgressView() {
        mPullProgressContainer = findViewById(R.id.pullProgressContainer);
        mPullProgressContainer.setVisibility(View.GONE);
        mPullProgressBar = (ProgressBar) mPullProgressContainer
                .findViewById(R.id.pullProgress);
        mPullMsg = (TextView) mPullProgressContainer.findViewById(R.id.pullMsg);
        mPullLeftHint = (TextView) mPullProgressContainer
                .findViewById(R.id.leftHint);
        mPullRightHint = (TextView) mPullProgressContainer
                .findViewById(R.id.rightHint);
    }

    private void setupActionBar() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mActionBar = getActionBar();
        mViewPagerAdapter = new TabItemPagerAdapter(getFragmentManager());

        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mActionBar.setSelectedNavigationItem(position);
                    }
                });
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setDisplayShowTitleEnabled(true);

        for (int textId : NAV_TABS) {
            ActionBar.Tab tab = mActionBar.newTab().setText(getString(textId))
                    .setTabListener(this);
            mActionBar.addTab(tab);
        }

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void createFragments() {
        mFilesFragment = FilesFragment.newInstance(mRepo);
        mCommitsFragment = CommitsFragment.newInstance(mRepo);
    }

    public void resetCommits(final String commitName) {
        CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName,
                new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        mFilesFragment.reset(commitName);
                        mCommitsFragment.reset(commitName);
                    }
                });
        checkoutTask.executeTask();
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
                finish();
                return true;
            case R.id.action_delete:
                deleteRepo();
                return true;
            case R.id.action_pull:
                pullRepo();
                return true;
            case R.id.action_diff:
                mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
                mCommitsFragment.enterDiffActionMode();
                return true;
            case R.id.action_merge:
                MergeDialog md = new MergeDialog();
                md.setArguments(mRepo.getBundle());
                md.show(getFragmentManager(), "merge-repo-dialog");
                return true;
            case R.id.action_push:
                PushRepoDialog prd = new PushRepoDialog();
                prd.show(getFragmentManager(), "push-repo-dialog");
                return true;
            case R.id.action_commit:
                showEditTextDialog(R.string.dialog_commit_title,
                        R.string.dialog_commit_msg_hint, R.string.label_commit,
                        new OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                commitChanges(text);
                            }
                        });
                return true;
            case R.id.action_reset:
                showMessageDialog(R.string.dialog_reset_commit_title,
                        R.string.dialog_reset_commit_msg,
                        R.string.action_reset,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                resetCommitChanges();
                            }
                        });
                return true;
            case R.id.action_new_dir:
                showEditTextDialog(R.string.dialog_create_dir_title,
                        R.string.dialog_create_dir_hint, R.string.label_create,
                        new OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                mFilesFragment.newDir(text);
                                reset();
                            }
                        });
                return true;
            case R.id.action_new_file:
                showEditTextDialog(R.string.dialog_create_file_title,
                        R.string.dialog_create_file_hint,
                        R.string.label_create, new OnEditTextDialogClicked() {
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

    private void reset() {
        mFilesFragment.reset();
        mCommitsFragment.reset();
    }

    public void setFilesFragment(FilesFragment filesFragment) {
        mFilesFragment = filesFragment;
    }

    public void setCommitsFragment(CommitsFragment commitsFragment) {
        mCommitsFragment = commitsFragment;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
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

    private class TabItemPagerAdapter extends FragmentPagerAdapter {

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
        public int getCount() {
            return NAV_TABS.length;
        }

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
                    .getItem(position).getOnBackClickListener();
            if (onBackClickListener != null) {
                if (onBackClickListener.onClick())
                    return true;
            }
            finish();
            return true;
        }
        return false;
    }

    public void mergeBranch(final Ref commit, final String ffModeStr,
            final boolean autoCommit) {
        MergeTask mergeTask = new MergeTask(mRepo, commit, ffModeStr,
                autoCommit, new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        reset();
                    }
                });
        mergeTask.executeTask();
    }

    public void commitChanges(String commitMsg) {
        CommitChangesTask commitTask = new CommitChangesTask(mRepo, commitMsg,
                new AsyncTaskPostCallback() {

                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        reset();
                        showToastMessage(R.string.toast_commit_success);
                    }
                });
        commitTask.executeTask();
    }

    private void resetCommitChanges() {
        ResetCommitTask resetTask = new ResetCommitTask(mRepo,
                new AsyncTaskPostCallback() {
                    @Override
                    public void onPostExecute(Boolean isSuccess) {
                        reset();
                        showToastMessage(R.string.toast_reset_success);
                    }
                });
        resetTask.executeTask();
    }

    public void error() {
        finish();
        showToastMessage(R.string.error_unknown);
    }

    private void deleteRepo() {
        showMessageDialog(R.string.dialog_delete_repo_title,
                R.string.dialog_delete_repo_msg, R.string.label_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mRepo.deleteRepo();
                        finish();
                    }
                });
    }

    public void pullRepo() {
        mPullMsg.setText(R.string.pull_msg_init);
        PullTask pullTask = new PullTask(mRepo, new ProgressCallback());
        pullTask.executeTask();
    }

    public void pushRepo(boolean pushAll) {
        mPullMsg.setText(R.string.push_msg_init);
        PushTask pushTask = new PushTask(mRepo, pushAll, new ProgressCallback());
        pushTask.executeTask();
    }

    private class ProgressCallback implements AsyncTaskCallback {

        @Override
        public void onPreExecute() {
            Animation anim = AnimationUtils.loadAnimation(
                    RepoDetailActivity.this, R.anim.fade_in);
            mPullProgressContainer.setAnimation(anim);
            mPullProgressContainer.setVisibility(View.VISIBLE);
            mPullLeftHint.setText(R.string.progress_left_init);
            mPullRightHint.setText(R.string.progress_right_init);
        }

        @Override
        public void onProgressUpdate(String... progress) {
            mPullMsg.setText(progress[0]);
            mPullLeftHint.setText(progress[1]);
            mPullRightHint.setText(progress[2]);
            mPullProgressBar.setProgress(Integer.parseInt(progress[3]));
        }

        @Override
        public void onPostExecute(Boolean isSuccess) {
            Animation anim = AnimationUtils.loadAnimation(
                    RepoDetailActivity.this, R.anim.fade_out);
            mPullProgressContainer.setAnimation(anim);
            mPullProgressContainer.setVisibility(View.GONE);
            reset();
        }

        @Override
        public boolean doInBackground(Void... params) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

    }
}
