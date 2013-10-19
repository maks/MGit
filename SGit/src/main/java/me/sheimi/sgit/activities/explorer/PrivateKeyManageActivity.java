package me.sheimi.sgit.activities.explorer;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.io.FileFilter;

import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.RenameKeyDialog;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.FsUtils;

public class PrivateKeyManageActivity extends FileExplorerActivity {

    private static final int REQUSET_ADD_KEY = 0;

    @Override
    protected File getRootFolder() {
        return FsUtils.getInstance(this).getDir("ssh");
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return null;
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int positon, long id) {
                File file = mFilesListAdapter.getItem(positon);
                FsUtils.getInstance(PrivateKeyManageActivity.this).openFile(file, "text/plain");
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long id) {
                File file = mFilesListAdapter.getItem(position);
                RenameKeyDialog rkd = new RenameKeyDialog(file.getAbsolutePath());
                rkd.show(getSupportFragmentManager(), "rename-dialog");
                return true;
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.private_key_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                File newKey = new File(getRootFolder(), keyFile.getName());
                FsUtils.getInstance(this).copyFile(keyFile, newKey);
                refreshList();
                break;
        }

    }

    public void refreshList() {
        setCurrentDir(getRootFolder());
        CommonUtils.getInstance(this).refreshSgitTransportCallback();
    }

}
