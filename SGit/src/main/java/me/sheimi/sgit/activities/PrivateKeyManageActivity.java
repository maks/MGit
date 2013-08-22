package me.sheimi.sgit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;

public class PrivateKeyManageActivity extends Activity {

    private File mPrivateKeyFolder;
    private FsUtils mFsUtils;
    private RepoUtils mRepoUtils;
    private ListView mPrivateKeyList;
    private FilesListAdapter mFilesListAdapter;

    private static final int REQUSET_ADD_KEY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        setupActionBar();
        mFsUtils = FsUtils.getInstance(this);
        mRepoUtils = RepoUtils.getInstance(this);
        mPrivateKeyList = (ListView) findViewById(R.id.fileList);
        mFilesListAdapter = new FilesListAdapter(this);
        mPrivateKeyList.setAdapter(mFilesListAdapter);
        mPrivateKeyFolder = mFsUtils.getDir("ssh");
        mFilesListAdapter.setDir(mPrivateKeyFolder);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.private_key_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtils.finishActivity(this);
                return true;
            case R.id.action_new:
                Intent intent = new Intent(this, ExploreFileActivity.class);
                startActivityForResult(intent, REQUSET_ADD_KEY);
                ActivityUtils.forwardTransition(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUSET_ADD_KEY:
                String path = data.getExtras().getString(ExploreFileActivity.RESULT_PATH);
                File keyFile = new File(path);
                File newKey = new File(mPrivateKeyFolder, keyFile.getName());
                mFsUtils.copyFile(keyFile, newKey);
                mFilesListAdapter.setDir(mPrivateKeyFolder);
                mRepoUtils.refreshSgitTransportCallback();
                break;
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

}
