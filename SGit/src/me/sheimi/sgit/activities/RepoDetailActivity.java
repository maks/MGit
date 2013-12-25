package me.sheimi.sgit.activities;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.adapters.RepoOperationsAdapter;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.MergeDialog;
import me.sheimi.sgit.dialogs.PushRepoDialog;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskCallback;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;
import me.sheimi.sgit.repo.tasks.repo.CheckoutTask;
import me.sheimi.sgit.repo.tasks.repo.CommitChangesTask;
import me.sheimi.sgit.repo.tasks.repo.MergeTask;
import me.sheimi.sgit.repo.tasks.repo.PullTask;
import me.sheimi.sgit.repo.tasks.repo.PushTask;
import me.sheimi.sgit.repo.tasks.repo.ResetCommitTask;

import org.eclipse.jgit.lib.Ref;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RepoDetailActivity extends SheimiFragmentActivity {

    private ActionBar mActionBar;

    private FilesFragment mFilesFragment;
    private CommitsFragment mCommitsFragment;
    private BaseFragment mCurrentFragment;
    private ListView mRightDrawer;
    private DrawerLayout mDrawerLayout;
    private RepoOperationsAdapter mDrawerAdapter;

    private Repo mRepo;

    private View mPullProgressContainer;
    private ProgressBar mPullProgressBar;
    private TextView mPullMsg;
    private TextView mPullLeftHint;
    private TextView mPullRightHint;

    private static final int FILE_FRAGMENT_INDEX = 0;
    private static final int COMMIT_FRAGMENT_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        setTitle(mRepo.getLocalPath());
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        createFragments();
        setupPullProgressView();
        setupDrawer();
    }

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawer = (ListView) findViewById(R.id.right_drawer);
        mDrawerAdapter = new RepoOperationsAdapter(this);
        mRightDrawer.setAdapter(mDrawerAdapter);
        mRightDrawer.setOnItemClickListener(mDrawerAdapter);
        selectFragment(mFilesFragment);
    }

    public void selectFragment(int position) {
        switch (position) {
            case FILE_FRAGMENT_INDEX:
                selectFragment(mFilesFragment);
                break;
            case COMMIT_FRAGMENT_INDEX:
                selectFragment(mCommitsFragment);
                break;
        }
    }

    private void selectFragment(BaseFragment fragment) {
        if (fragment == mCurrentFragment)
            return;
        mCurrentFragment = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mCurrentFragment).commit();
        mDrawerLayout.closeDrawer(mRightDrawer);
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
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(true);
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCurrentFragment == null) {
                return false;
            }
            OnBackClickListener onBackClickListener = mCurrentFragment
                    .getOnBackClickListener();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_toggle_drawer:
                if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                    mDrawerLayout.closeDrawer(mRightDrawer);
                } else {
                    mDrawerLayout.openDrawer(mRightDrawer);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectRepoOperation(int option) {
        switch (option) {
            case R.string.action_delete:
                deleteRepo();
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_pull:
                pullRepo();
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_diff:
                selectFragment(mCommitsFragment); // TODO
                mCommitsFragment.enterDiffActionMode();
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_merge:
                MergeDialog md = new MergeDialog();
                md.setArguments(mRepo.getBundle());
                md.show(getFragmentManager(), "merge-repo-dialog");
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_push:
                PushRepoDialog prd = new PushRepoDialog();
                prd.show(getFragmentManager(), "push-repo-dialog");
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_commit:
                showEditTextDialog(R.string.dialog_commit_title,
                        R.string.dialog_commit_msg_hint, R.string.label_commit,
                        new OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                commitChanges(text);
                            }
                        });
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_reset:
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
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_new_dir:
                showEditTextDialog(R.string.dialog_create_dir_title,
                        R.string.dialog_create_dir_hint, R.string.label_create,
                        new OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                mFilesFragment.newDir(text);
                                reset();
                            }
                        });
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
            case R.string.action_new_file:
                showEditTextDialog(R.string.dialog_create_file_title,
                        R.string.dialog_create_file_hint,
                        R.string.label_create, new OnEditTextDialogClicked() {
                            @Override
                            public void onClicked(String text) {
                                mFilesFragment.newFile(text);
                                reset();
                            }
                        });
                mDrawerLayout.closeDrawer(mRightDrawer);
                break;
        }
    }
}
