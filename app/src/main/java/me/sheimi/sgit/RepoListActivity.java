package me.sheimi.sgit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.manichord.mgit.transport.MGitHttpConnectionFactory;
import com.manichord.mgit.transport.SSLProviderInstaller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.activities.UserSettingsActivity;
import me.sheimi.sgit.activities.explorer.ExploreFileActivity;
import me.sheimi.sgit.activities.explorer.ImportRepositoryActivity;
import me.sheimi.sgit.adapters.RepoListAdapter;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.CloneDialog;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.dialogs.ImportLocalRepoDialog;
import me.sheimi.sgit.repo.tasks.repo.CloneTask;
import me.sheimi.sgit.ssh.PrivateKeyUtils;
import timber.log.Timber;

public class RepoListActivity extends SheimiFragmentActivity {

    private ListView mRepoList;
    private Context mContext;
    private RepoListAdapter mRepoListAdapter;

    private static final int REQUEST_IMPORT_REPO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrivateKeyUtils.migratePrivateKeys();

        initUpdatedSSL();

        setContentView(R.layout.activity_main);
        mRepoList = (ListView) findViewById(R.id.repoList);
        mRepoListAdapter = new RepoListAdapter(this);
        mRepoList.setAdapter(mRepoListAdapter);
        mRepoListAdapter.queryAllRepo();
        mRepoList.setOnItemClickListener(mRepoListAdapter);
        mRepoList.setOnItemLongClickListener(mRepoListAdapter);

        checkPermission();

        mContext = getApplicationContext();

        Uri uri = this.getIntent().getData();
        if(uri != null){
            URL mRemoteRepoUrl = null;
            try {
                mRemoteRepoUrl = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
            } catch (MalformedURLException e) {
                Toast.makeText(mContext, R.string.invalid_url, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            if(mRemoteRepoUrl != null){
                String remoteUrl = mRemoteRepoUrl.toString();
                String repoName = remoteUrl.substring(remoteUrl.lastIndexOf("/")+1);
                StringBuilder repoUrlBuilder = new StringBuilder(remoteUrl);

                //need git extension to clone some repos
                if(!remoteUrl.toLowerCase().endsWith(getString(R.string.git_extension)))
                {
                    repoUrlBuilder.append(getString(R.string.git_extension));
                }
                else//if has git extension remove it from repository name
                    {
                        repoName = repoName.substring(0, repoName.lastIndexOf('.'));
                    }
                //Check if there are others repositories with same remote
                List<Repo> repositoriesWithSameRemote = Repo.getRepoList(mContext,  RepoDbManager.searchRepo(remoteUrl));

                //if so, just open it
                if(repositoriesWithSameRemote.size() > 0){
                    Toast.makeText(mContext, R.string.repository_already_present, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, RepoDetailActivity.class);
                    intent.putExtra(Repo.TAG, repositoriesWithSameRemote.get(0));
                    startActivity(intent);
                }
                else{
					final String cloningStatus = getString(R.string.cloning);
	                Repo mRepo = Repo.createRepo(repoName, repoUrlBuilder.toString(), cloningStatus );
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
                CloneDialog cloneDialog = new CloneDialog();
                cloneDialog.show(getSupportFragmentManager(), "clone-dialog");
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

    private void checkPermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                */
                int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
                ActivityCompat.requestPermissions(this,
                        new String[]{ permission },
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            //}
        }
    }

    private void initUpdatedSSL() {
        if (Build.VERSION.SDK_INT < 21) {
            SSLProviderInstaller.install(this);
        }
        MGitHttpConnectionFactory.install();
        Timber.i("Installed custom HTTPS factory");
    }
}
