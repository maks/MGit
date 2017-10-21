package me.sheimi.sgit.activities.explorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileFilter;

import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.ViewFileActivity;
import me.sheimi.sgit.dialogs.EditKeyPasswordDialog;
import me.sheimi.sgit.dialogs.RenameKeyDialog;
import me.sheimi.sgit.ssh.PrivateKeyUtils;

public class PrivateKeyManageActivity extends FileExplorerActivity implements ActionMode.Callback {

    private static final int REQUEST_IMPORT_KEY = 0;

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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_mode_ssh_key, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_mode_rename_key:
	    Bundle pathArg = new Bundle();
	    pathArg.putString(RenameKeyDialog.FROM_PATH,
			      mChosenFile.getAbsolutePath());
	    mode.finish();
	    RenameKeyDialog rkd = new RenameKeyDialog();
	    rkd.setArguments(pathArg);
	    rkd.show(getSupportFragmentManager(), "rename-dialog");
	    return true;
	case R.id.action_mode_show_private_key: {
		Intent intent = new Intent(PrivateKeyManageActivity.this, ViewFileActivity.class);
		intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
				mChosenFile.getAbsolutePath());
		intent.putExtra(ViewFileActivity.TAG_MODE, ViewFileActivity.TAG_MODE_SSH_KEY);
		mode.finish();
		startActivity(intent);
		return true;
	}
	case R.id.action_mode_show_public_key:
		Intent intent = new Intent(PrivateKeyManageActivity.this, ViewFileActivity.class);
		intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
				PrivateKeyUtils.getPublicKeyEnsure(mChosenFile).getAbsolutePath());
		intent.putExtra(ViewFileActivity.TAG_MODE, ViewFileActivity.TAG_MODE_SSH_KEY);
		mode.finish();
		startActivity(intent);
		return true;
	case R.id.action_mode_edit_key_password:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.dialog_not_supported)
                .setMessage(R.string.dialog_not_supported_msg)
                .setPositiveButton(R.string.label_ok, null)
                .show();
        } else {
            pathArg = new Bundle();
            pathArg.putString(EditKeyPasswordDialog.KEY_FILE_EXTRA, mChosenFile.getAbsolutePath());
            mode.finish();
            EditKeyPasswordDialog editDialog = new EditKeyPasswordDialog();
            editDialog.setArguments(pathArg);
            editDialog.show(getSupportFragmentManager(), "rename-dialog");
        }
        return true;
	case R.id.action_mode_delete:
	    mode.finish();
	    new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.dialog_key_delete)
		.setMessage(getString(R.string.dialog_key_delete_msg) + " " + mChosenFile)
		.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			    FsUtils.deleteFile(mChosenFile);
			    FsUtils.deleteFile(PrivateKeyUtils.getPublicKey(mChosenFile));
			    refreshList();
			}

        		    })
		.setNegativeButton(R.string.label_cancel, null)
		.show();
	    return true;

	default:
	    return false;
	}
    }

    private boolean mInActionMode;
    private File mChosenFile;

    @Override
    public void onDestroyActionMode(ActionMode mode) {
	    mInActionMode = false;
        mFilesListAdapter.notifyDataSetChanged();
    }

    private void runActionMode(View view, int positon) {
	if (mInActionMode) {
	    return;
	}

	mInActionMode = true;
	mChosenFile = mFilesListAdapter.getItem(positon);
	PrivateKeyManageActivity.this.startActionMode(PrivateKeyManageActivity.this);
	view.setSelected(true);
        mFilesListAdapter.notifyDataSetChanged();
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                Intent intent = new Intent(PrivateKeyManageActivity.this, ViewFileActivity.class);
                intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
                        PrivateKeyUtils.getPublicKeyEnsure(mFilesListAdapter.getItem(position))
                                .getAbsolutePath());
                intent.putExtra(ViewFileActivity.TAG_MODE, ViewFileActivity.TAG_MODE_SSH_KEY);
                startActivity(intent);
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView,
                    View view, int position, long id) {
		runActionMode(view, position);
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
        case R.id.action_import:
            {
                Intent intent = new Intent(this, ExploreFileActivity.class);
                startActivityForResult(intent, REQUEST_IMPORT_KEY);
                forwardTransition();
                return true;
            }
        case R.id.action_generate:
            {
                (new PrivateKeyGenerate()).show(getSupportFragmentManager(), "generate-key");
                refreshList();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
	case REQUEST_IMPORT_KEY:
	    {
                String path = data.getExtras().getString(
                        ExploreFileActivity.RESULT_PATH);
                File keyFile = new File(path);
                File newKey = new File(getRootFolder(), keyFile.getName());
                FsUtils.copyFile(keyFile, newKey);
                refreshList();
                break;
	    }
        }

    }

    public void refreshList() {
        setCurrentDir(getRootFolder());
    }

}
