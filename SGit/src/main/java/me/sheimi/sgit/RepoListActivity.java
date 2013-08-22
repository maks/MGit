package me.sheimi.sgit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import java.io.File;

import me.sheimi.sgit.activities.PrivateKeyManageActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.RepoListAdapter;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.DeleteRepoDialog;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

public class RepoListActivity extends FragmentActivity {

    private ListView mRepoList;
    private RepoListAdapter mRepoListAdapter;
    private ViewUtils mViewUtils;
    private FsUtils mFsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewUtils = ViewUtils.getInstance(this);
        mFsUtils = FsUtils.getInstance(this);
        mRepoList = (ListView) findViewById(R.id.repoList);
        mRepoListAdapter = new RepoListAdapter(this);
        mRepoList.setAdapter(mRepoListAdapter);
        mRepoListAdapter.queryAllRepo();

        mRepoList.setOnItemClickListener(new AdapterView.OnItemClickListener
                () {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                Repo repo = mRepoListAdapter.getItem(position);
                if (repo.getRepoStatus() != RepoContract.REPO_STATUS_NULL)
                    return;
                Intent intent = new Intent(RepoListActivity.this,
                        RepoDetailActivity.class);
                intent.putExtra(RepoContract.RepoEntry._ID, repo.getID());
                ActivityUtils.startActivity(RepoListActivity.this, intent);
            }
        });
        mRepoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long id) {
                Repo repo = mRepoListAdapter.getItem(position);
                DeleteRepoDialog drd = new DeleteRepoDialog(repo.getID(), repo.getLocalPath(),
                        null);
                drd.show(getSupportFragmentManager(), "delete-repo-dialog");
                return false;
            }
        });
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
            case R.id.action_add_private_key:
                intent = new Intent(this,
                        PrivateKeyManageActivity.class);
                ActivityUtils.startActivity(this, intent);
                return true;
            case R.id.action_new:
                CloneDialog cloneDialog = new CloneDialog();
                cloneDialog.show(getSupportFragmentManager(), "clone-dialog");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void configSearchAction(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null)
            return;
        SearchListener searchListener = new SearchListener();
        searchItem.setOnActionExpandListener(searchListener);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(searchListener);
    }

    public class SearchListener implements SearchView.OnQueryTextListener,
            MenuItem.OnActionExpandListener {

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

    public class CloneDialog extends DialogFragment implements View.OnClickListener {

        private EditText mRemoteURL;
        private EditText mLocalPath;
        private EditText mUsername;
        private EditText mPassword;
        private Activity mActivity;
        private RepoUtils mRepoUtils;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            mActivity = getActivity();
            mRepoUtils = RepoUtils.getInstance(mActivity);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_clone, null);
            builder.setView(layout);

            mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
            mLocalPath = (EditText) layout.findViewById(R.id.localPath);
            mUsername = (EditText) layout.findViewById(R.id.username);
            mPassword = (EditText) layout.findViewById(R.id.password);

            mRemoteURL.setText(RepoUtils.TEST_REPO);
            mLocalPath.setText(RepoUtils.TEST_LOCAL);
            mUsername.setText(RepoUtils.TEST_USERNAME);
            mPassword.setText(RepoUtils.TEST_PASSWORD);

            // set button listener
            builder.setTitle(R.string.title_clone_repo);
            builder.setNegativeButton(getString(R.string.label_cancel), new DummyDialogListener());
            builder.setPositiveButton(getString(R.string.label_clone), new DummyDialogListener());

            return builder.create();
        }


        @Override
        public void onStart() {
            super.onStart();
            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog == null)
                return;
            Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String remoteURL = mRemoteURL.getText().toString().trim();
            String localPath = mLocalPath.getText().toString().trim();

            if (remoteURL.equals("")) {
                mViewUtils.showToastMessage(R.string.alert_remoteurl_required);
                mRemoteURL.setError(getString(R.string.alert_remoteurl_required));
                return;
            }
            if (localPath.equals("")) {
                mViewUtils.showToastMessage(R.string.alert_localpath_required);
                mLocalPath.setError(getString(R.string.alert_localpath_required));
                return;
            }
            if (localPath.contains("/")) {
                mViewUtils.showToastMessage(R.string.alert_localpath_format);
                mLocalPath.setError(getString(R.string.alert_localpath_format));
                return;
            }

            File file = mFsUtils.getRepo(localPath);
            if (file.exists()) {
                mViewUtils.showToastMessage(R.string.alert_localpath_repo_exists);
                mLocalPath.setError(getString(R.string.alert_localpath_repo_exists));
                return;
            }

            String username = mUsername.getText().toString();
            String password = mPassword.getText().toString();
            ContentValues values = new ContentValues();
            values.put(RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH, localPath);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL, remoteURL);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                    RepoContract.REPO_STATUS_WAITING_CLONE);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_USERNAME, username);
            values.put(RepoContract.RepoEntry.COLUMN_NAME_PASSWORD, password);
            long id = RepoDbManager.getInstance(mActivity)
                    .insertRepo(values);
            cloneRepo(id, remoteURL, localPath, username, password);
            dismiss();
        }

        public void cloneRepo(final long id, final String remoteUrl, final String localPath,
                              final String username, final String password) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mRepoUtils.cloneSync(remoteUrl, localPath, username, password,
                                mRepoListAdapter.new CloningMonitor(id));
                        Git git = mRepoUtils.getGit(localPath);
                        mRepoUtils.checkoutAllGranches(git);
                        mRepoUtils.updateLatestCommitInfo(git, id);
                        ContentValues values = new ContentValues();
                        values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                                RepoContract.REPO_STATUS_NULL);
                        RepoDbManager.getInstance(mActivity).updateRepo(id, values);
                    } catch (GitAPIException e) {
                        RepoDbManager.getInstance(mActivity).deleteRepo(id);
                    } catch (JGitInternalException e) {
                        RepoDbManager.getInstance(mActivity).deleteRepo(id);
                    }
                }
            });
            thread.start();
        }

    }

}
