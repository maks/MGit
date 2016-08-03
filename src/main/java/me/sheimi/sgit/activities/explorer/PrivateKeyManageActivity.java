package me.sheimi.sgit.activities.explorer;

import java.io.File;
import java.io.FileFilter;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.RenameKeyDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import me.sheimi.sgit.ssh.PrivateKeyUtils;

public class PrivateKeyManageActivity extends FileExplorerActivity {

    private static final int REQUSET_ADD_KEY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BasicFunctions.setActiveActivity(this);
        PrivateKeyUtils.migratePrivateKeys();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected File getRootFolder() {
        return PrivateKeyUtils.getPrivateKeyFolder();
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return null;
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                    int positon, long id) {
                File file = mFilesListAdapter.getItem(positon);
                FsUtils.openFile(file, "text/plain");
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView,
                    View view, int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                Bundle pathArg = new Bundle();
                pathArg.putString(RenameKeyDialog.FROM_PATH,
                        file.getAbsolutePath());
                RenameKeyDialog rkd = new RenameKeyDialog();
                rkd.setArguments(pathArg);
                rkd.show(getFragmentManager(), "rename-dialog");
                return true;
            }
        };
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
            case R.id.action_new:
                Intent intent = new Intent(this, ExploreFileActivity.class);
                startActivityForResult(intent, REQUSET_ADD_KEY);
                forwardTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUSET_ADD_KEY:
                String path = data.getExtras().getString(
                        ExploreFileActivity.RESULT_PATH);
                File keyFile = new File(path);
                File newKey = new File(getRootFolder(), keyFile.getName());
                FsUtils.copyFile(keyFile, newKey);
                refreshList();
                break;
        }

    }

    public void refreshList() {
        setCurrentDir(getRootFolder());
    }

}
