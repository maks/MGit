package me.sheimi.sgit.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.SGitApplication;
import me.sheimi.sgit.activities.delegate.RepoOperationDelegate;
import me.sheimi.sgit.activities.delegate.actions.PullAction;
import me.sheimi.sgit.adapters.RepoOperationsAdapter;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.fragments.StatusFragment;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskCallback;

public class RepoDetailActivity extends SheimiFragmentActivity {

    private ActionBar mActionBar;

    private FilesFragment mFilesFragment;
    private CommitsFragment mCommitsFragment;
    private StatusFragment mStatusFragment;

    private RelativeLayout mRightDrawer;
    private ListView mRepoOperationList;
    private DrawerLayout mDrawerLayout;
    private RepoOperationsAdapter mDrawerAdapter;
    private TabItemPagerAdapter mTabItemPagerAdapter;
    private ViewPager mViewPager;
    private Button mCommitNameButton;
    private ImageView mCommitType;
    private MenuItem mSearchItem;

    private Repo mRepo;

    private View mPullProgressContainer;
    private ProgressBar mPullProgressBar;
    private TextView mPullMsg;
    private TextView mPullLeftHint;
    private TextView mPullRightHint;

    private RepoOperationDelegate mRepoDelegate;

    private static final int FILES_FRAGMENT_INDEX = 0;
    private static final int COMMITS_FRAGMENT_INDEX = 1;
    private static final int STATUS_FRAGMENT_INDEX = 2;
    private static final int BRANCH_CHOOSE_ACTIVITY = 0;
    private int mSelectedTab;
    private boolean execExternalCommand;

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	switch (requestCode) {
	case BRANCH_CHOOSE_ACTIVITY:
	    String branchName = mRepo.getBranchName();
	    if (branchName == null) {
		showToastMessage(R.string.error_something_wrong);
		return;
	    }
	    reset(branchName);
	    break;
	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        // aweful hack! workaround for null repo when returning from BranchChooser, but going to
        // shortly refactor passing in serialised repo, so not worth doing more to fix for now
        if (mRepo == null) {
            mRepo = checkExternalCommand(getIntent());
            if (mRepo != null) {
                execExternalCommand = true;
            } else {
                finish();
                return;
            }
        }
        repoInit();
        setTitle(mRepo.getDisplayName());
        setContentView(R.layout.activity_repo_detail);
        setupActionBar();
        createFragments();
        setupViewPager();
        setupPullProgressView();
        setupDrawer();
        mCommitNameButton = (Button) findViewById(R.id.commitName);
        mCommitType = (ImageView) findViewById(R.id.commitType);
        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
		Intent intent = new Intent(RepoDetailActivity.this, BranchChooserActivity.class);
		intent.putExtra(Repo.TAG, mRepo);
		startActivityForResult(intent, BRANCH_CHOOSE_ACTIVITY);
            }
        });
        String branchName = mRepo.getBranchName();
        if (branchName == null) {
            showToastMessage(R.string.error_something_wrong);
            return;
        }
        resetCommitButtonName(branchName);

        if (execExternalCommand) {
            syncRepo(getIntent());
        }
    }

    public RepoOperationDelegate getRepoDelegate() {
        if (mRepoDelegate == null) {
            mRepoDelegate = new RepoOperationDelegate(mRepo, this);
        }
        return mRepoDelegate;
    }

    private void setupViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabItemPagerAdapter = new TabItemPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabItemPagerAdapter);
        mViewPager.setOnPageChangeListener(mTabItemPagerAdapter);
    }

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawer = (RelativeLayout) findViewById(R.id.right_drawer);
        mRepoOperationList = (ListView) findViewById(R.id.repoOperationList);
        mDrawerAdapter = new RepoOperationsAdapter(this);
        mRepoOperationList.setAdapter(mDrawerAdapter);
        mRepoOperationList.setOnItemClickListener(mDrawerAdapter);
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
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void createFragments() {
        mFilesFragment = FilesFragment.newInstance(mRepo);
        mCommitsFragment = CommitsFragment.newInstance(mRepo, null);
        mStatusFragment = StatusFragment.newInstance(mRepo);
    }

    private void resetCommitButtonName(String commitName) {
        int commitType = Repo.getCommitType(commitName);
        switch (commitType) {
            case Repo.COMMIT_TYPE_REMOTE:
                // change the display name to local branch
                commitName = Repo.convertRemoteName(commitName);
            case Repo.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case Repo.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case Repo.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = Repo.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
    }

    public void reset(String commitName) {
        resetCommitButtonName(commitName);
        reset();
    }

    public void reset() {
        mFilesFragment.reset();
        mCommitsFragment.reset();
        mStatusFragment.reset();
    }

    public void setFilesFragment(FilesFragment filesFragment) {
        mFilesFragment = filesFragment;
    }

    public FilesFragment getFilesFragment() {
        return mFilesFragment;
    }

    public void setCommitsFragment(CommitsFragment commitsFragment) {
        mCommitsFragment = commitsFragment;
    }

    public void setStatusFragment(StatusFragment statusFragment) {
        mStatusFragment = statusFragment;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repo_detail, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, mTabItemPagerAdapter);
        mSearchItem.setVisible(mSelectedTab == COMMITS_FRAGMENT_INDEX);
        SearchView searchView = (SearchView) mSearchItem.getActionView();
        if (searchView != null) {
            searchView.setIconifiedByDefault(true);
            searchView.setOnQueryTextListener(mTabItemPagerAdapter);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DEL:
                int position = mViewPager.getCurrentItem();
                OnBackClickListener onBackClickListener = mTabItemPagerAdapter
                        .getItem(position).getOnBackClickListener();
                if (onBackClickListener != null) {
                    if (onBackClickListener.onClick())
                        return true;
                }
                finish();
                return true;
            case KeyEvent.KEYCODE_F:
                mViewPager.setCurrentItem(FILES_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_C:
                mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_S:
                mViewPager.setCurrentItem(STATUS_FRAGMENT_INDEX);
                return true;
            case KeyEvent.KEYCODE_SLASH:
                if (event.isShiftPressed()) {
                    showKeyboardShortcutsHelpOverlay();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void showKeyboardShortcutsHelpOverlay() {
        showMessageDialog(R.string.dialog_keymap_title, getString(R.string.dialog_keymap_mesg));
    }

    public void error() {
        finish();
        showToastMessage(R.string.error_unknown);
    }

    public class ProgressCallback implements AsyncTaskCallback {

        private int mInitMsg;

        public ProgressCallback(int initMsg) {
            mInitMsg = initMsg;
        }

        @Override
        public void onPreExecute() {
            mPullMsg.setText(mInitMsg);
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

    public void closeOperationDrawer() {
        mDrawerLayout.closeDrawer(mRightDrawer);
    }

    public void enterDiffActionMode() {
        mViewPager.setCurrentItem(COMMITS_FRAGMENT_INDEX);
        mCommitsFragment.enterDiffActionMode();
    }

    private void repoInit() {
        mRepo.updateLatestCommitInfo();
        mRepo.getRemotes();
    }

    class TabItemPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

        private final int[] PAGE_TITLE = { R.string.tab_files_label,
                R.string.tab_commits_label, R.string.tab_status_label };

        public TabItemPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFragment getItem(int position) {
            switch (position) {
                case FILES_FRAGMENT_INDEX:
                    return mFilesFragment;
                case COMMITS_FRAGMENT_INDEX:
                    return mCommitsFragment;
                case STATUS_FRAGMENT_INDEX:
                    mStatusFragment.reset();
                    return mStatusFragment;
            }
            return mFilesFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(PAGE_TITLE[position]);
        }

        @Override
        public int getCount() {
            return PAGE_TITLE.length;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mSelectedTab = position;
            if (mSearchItem != null) {
                mSearchItem.setVisible(position == COMMITS_FRAGMENT_INDEX);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            switch (mViewPager.getCurrentItem()) {
                case COMMITS_FRAGMENT_INDEX:
                    mCommitsFragment.setFilter(query);
                    break;
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            switch (mViewPager.getCurrentItem()) {
                case COMMITS_FRAGMENT_INDEX:
                    mCommitsFragment.setFilter(query);
                    break;
            }
            return true;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            switch (mViewPager.getCurrentItem()) {
                case COMMITS_FRAGMENT_INDEX:
                    mCommitsFragment.setFilter(null);
                    break;
            }
            return true;
        }

    }

    private Repo checkExternalCommand(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            Toast.makeText(this, "Ошибка получения расположения репозитория", Toast.LENGTH_LONG).show();
            return null;
        }
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "Ошибка получения расположения репозитория", Toast.LENGTH_LONG).show();
            return null;
        }
//        String repoName = path.substring(path.lastIndexOf("/") + 1);
//        if (!Repo.getDir(((SGitApplication) getApplicationContext()).getPrefenceHelper(), repoName).exists()) {
        Repo repo = getRepoByName(path);
        if (repo == null) {
            Toast.makeText(this, "Репозиторий " + path + " отсутствует в списке добавленных",
                Toast.LENGTH_LONG).show();
            return null;
        }
        return repo;
    }

    private Repo getRepoByName(String localPath) {
//        ((SGitApplication) getApplicationContext()).getPrefenceHelper().setRepoRoot(
//            "/storage/sdcard0/Android/data/com.manichord.mgit.debug/files/repo");
        File file = ((SGitApplication) getApplicationContext()).getPrefenceHelper().getRepoRoot();
        // check repo path
        if (file == null || !localPath.startsWith(file.getAbsolutePath())) {
            return null;
        }
        // check repo name
        String repoName = new File(localPath).getName();
        Cursor cursor = RepoDbManager.searchRepo(repoName);
        List<Repo> repos = Repo.getRepoList(this, cursor);
        for (Repo repo : repos) {
            if (repoName.equalsIgnoreCase(repo.getLocalPath())) {
                return repo;
            }
        }
        return null;
    }

    private void syncRepo(@NotNull Intent intent) {
        String command = intent.getStringExtra(Intent.EXTRA_TEXT);
        syncRepo(command);
    }

    private void syncRepo(String command) {
        if (!TextUtils.isEmpty(command)) {
            String[] words = command.split(" ");
            if (words.length == 0) {
                return;
            }
            int operIndex = 0;
            if (words[0].equalsIgnoreCase("git"))
                operIndex = 1;

            if (operIndex == 1 && words.length == 1)
                return;
            if (words[operIndex].equalsIgnoreCase("pull")) {
                int forceIndex = findParam(words, new String[] {"-f", "--force"}, operIndex+1);
                boolean forcePull = (forceIndex != -1);
                String remote = null;
                if (forceIndex < words.length - 1)
                    remote = words[words.length - 1];

                if (forcePull && !TextUtils.isEmpty(remote)) {
                    PullAction.pull(mRepo, this, remote, forcePull);
                } else {
                    new PullAction(mRepo, this).execute();
                }
            }
        } else {
            Toast.makeText(this, "Не передана команда", Toast.LENGTH_LONG).show();
        }
    }

    private int findParam(String[] words, String param, int startPos) {
        if (words == null || startPos >= words.length)
            return -1;
        for (int i = startPos; i < words.length; i++) {
            if (words[i].equalsIgnoreCase(param))
                return i;
        }
        return -1;
    }

    private int findParam(String[] words, String[] params, int startPos) {
        if (words == null || startPos >= words.length)
            return -1;
        for (int i = startPos; i < words.length; i++) {
            for (String param : params) {
                if (words[i].equalsIgnoreCase(param)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void onSyncRepoFinish() {
//        Intent resIntent = new Intent("com.gee12.mytetroid.RESULT_ACTION");
//        setResult(Activity.RESULT_OK, resIntent);
        setResult(Activity.RESULT_OK);
        finish();
    }
}
