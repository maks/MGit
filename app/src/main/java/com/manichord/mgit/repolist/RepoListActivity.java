package com.manichord.mgit.repolist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.manichord.mgit.ViewHelperKt;
import com.manichord.mgit.clone.CloneViewModel;
import com.manichord.mgit.common.OnActionClickListener;
import com.manichord.mgit.transport.MGitHttpConnectionFactory;
import com.manichord.mgit.transport.SSLProviderInstaller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.SGitApplication;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.activities.UserSettingsActivity;
import me.sheimi.sgit.activities.explorer.ExploreFileActivity;
import me.sheimi.sgit.activities.explorer.ImportRepositoryActivity;
import me.sheimi.sgit.adapters.RepoListAdapter;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.databinding.ActivityMainBinding;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.dialogs.ImportLocalRepoDialog;
import me.sheimi.sgit.repo.tasks.repo.CloneTask;
import me.sheimi.sgit.ssh.PrivateKeyUtils;
import timber.log.Timber;

public class RepoListActivity extends SheimiFragmentActivity {

    private Context mContext;
    private RepoListAdapter mRepoListAdapter;

    private static final int REQUEST_IMPORT_REPO = 0;

    private ActivityMainBinding binding;

    public enum ClickActions {
        CLONE, CANCEL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAndRequestRequiredPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        RepoListViewModel viewModel = ViewModelProviders.of(this).get(RepoListViewModel.class);
        CloneViewModel cloneViewModel = ViewModelProviders.of(this).get(CloneViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setCloneViewModel(cloneViewModel);
        binding.setViewModel(viewModel);
        binding.setClickHandler(new OnActionClickListener() {
            @Override
            public void onActionClick(String action) {
                if (ClickActions.CLONE.name().equals(action)) {
                    cloneRepo();
                } else {
                    hideCloneView();
                }
            }
        });

        PrivateKeyUtils.migratePrivateKeys();

        initUpdatedSSL();

        mRepoListAdapter = new RepoListAdapter(this);
        binding.repoList.setAdapter(mRepoListAdapter);
        mRepoListAdapter.queryAllRepo();
        binding.repoList.setOnItemClickListener(mRepoListAdapter);
        binding.repoList.setOnItemLongClickListener(mRepoListAdapter);
        mContext = getApplicationContext();

        Uri uri = this.getIntent().getData();
        if (uri != null) {
            URL mRemoteRepoUrl = null;
            try {
                mRemoteRepoUrl = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
            } catch (MalformedURLException e) {
                Toast.makeText(mContext, R.string.invalid_url, Toast.LENGTH_LONG).show();
                Timber.e(e);
            }

            if (mRemoteRepoUrl != null) {
                String remoteUrl = mRemoteRepoUrl.toString();
                String repoName = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
                StringBuilder repoUrlBuilder = new StringBuilder(remoteUrl);

                //need git extension to clone some repos
                if (!remoteUrl.toLowerCase().endsWith(getString(R.string.git_extension))) {
                    repoUrlBuilder.append(getString(R.string.git_extension));
                } else { //if has git extension remove it from repository name
                    repoName = repoName.substring(0, repoName.lastIndexOf('.'));
                }
                //Check if there are others repositories with same remote
                List<Repo> repositoriesWithSameRemote = Repo.getRepoList(mContext, RepoDbManager.searchRepo(remoteUrl));

                //if so, just open it
                if (repositoriesWithSameRemote.size() > 0) {
                    Toast.makeText(mContext, R.string.repository_already_present, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, RepoDetailActivity.class);
                    intent.putExtra(Repo.TAG, repositoriesWithSameRemote.get(0));
                    startActivity(intent);
                } else if (Repo.getDir(((SGitApplication) getApplicationContext()).getPrefenceHelper(), repoName).exists()) {
                    // Repository with name end already exists, see https://github.com/maks/MGit/issues/289
                    cloneViewModel.setRemoteUrl(repoUrlBuilder.toString());
                    showCloneView();
                } else {
                    final String cloningStatus = getString(R.string.cloning);
                    Repo mRepo = Repo.createRepo(repoName, repoUrlBuilder.toString(), cloningStatus);
                    Boolean isRecursive = true;
                    CloneTask task = new CloneTask(mRepo, true, cloningStatus, null);
                    task.executeTask();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        configSearchAction(searchItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_new:
                showCloneView();
                return true;
            case R.id.action_import_repo:
                intent = new Intent(this, ImportRepositoryActivity.class);
                startActivityForResult(intent, REQUEST_IMPORT_REPO);
                forwardTransition();
                return true;
            case R.id.action_settings:
                intent = new Intent(this, UserSettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void configSearchAction(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null)
            return;
        SearchListener searchListener = new SearchListener();
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(searchListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_IMPORT_REPO:
                final String path = data.getExtras().getString(
                        ExploreFileActivity.RESULT_PATH);
                File file = new File(path);
                File dotGit = new File(file, Repo.DOT_GIT_DIR);
                if (!dotGit.exists()) {
                    showToastMessage(getString(R.string.error_no_repository));
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this);
                builder.setTitle(R.string.dialog_comfirm_import_repo_title);
                builder.setMessage(R.string.dialog_comfirm_import_repo_msg);
                builder.setNegativeButton(R.string.label_cancel,
                        new DummyDialogListener());
                builder.setPositiveButton(R.string.label_import,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                Bundle args = new Bundle();
                                args.putString(ImportLocalRepoDialog.FROM_PATH, path);
                                ImportLocalRepoDialog rld = new ImportLocalRepoDialog();
                                rld.setArguments(args);
                                rld.show(getSupportFragmentManager(), "import-local-dialog");
                            }
                        });
                builder.show();
                break;
        }
    }

    public class SearchListener implements SearchView.OnQueryTextListener,
            MenuItemCompat.OnActionExpandListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mRepoListAdapter.searchRepo(s);
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            mRepoListAdapter.queryAllRepo();
            return true;
        }

    }

    public void finish() {
        rawfinish();
    }

    private void initUpdatedSSL() {
        if (Build.VERSION.SDK_INT < 21) {
            SSLProviderInstaller.install(this);
        }
        MGitHttpConnectionFactory.install();
        Timber.i("Installed custom HTTPS factory");
    }

    private void cloneRepo() {
        if (binding.getCloneViewModel().validate()) {
            hideCloneView();
            binding.getCloneViewModel().cloneRepo();
        }
    }

    private void showCloneView() {
        binding.getCloneViewModel().show(true);
    }

    private void hideCloneView() {
        binding.getCloneViewModel().show(false);
        ViewHelperKt.hideKeyboard(this);
    }
}
