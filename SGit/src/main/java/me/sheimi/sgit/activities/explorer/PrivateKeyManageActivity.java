package me.sheimi.sgit.activities.explorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.io.FileFilter;

import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

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
                RenameKeyDialog rkd = new RenameKeyDialog(file);
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

    private class RenameKeyDialog extends DialogFragment implements View.OnClickListener,
            DialogInterface.OnClickListener {

        private File mFile;
        private EditText mNewFilename;
        private ViewUtils mViewUtils;

        public RenameKeyDialog(File file) {
            mFile = file;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            mViewUtils = ViewUtils.getInstance(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.dialog_rename_key_title));
            View view = PrivateKeyManageActivity.this.getLayoutInflater().inflate(R.layout
                    .dialog_rename_key, null);

            builder.setView(view);
            mNewFilename = (EditText) view.findViewById(R.id.newFilename);
            mNewFilename.setText(mFile.getName());

            // set button listener
            builder.setNegativeButton(R.string.label_cancel, new DummyDialogListener());
            builder.setNeutralButton(R.string.label_delete, this);
            builder.setPositiveButton(R.string.label_rename, new DummyDialogListener());

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
            String newFilename = mNewFilename.getText().toString().trim();
            if (newFilename.equals("")) {
                mViewUtils.showToastMessage(R.string.alert_new_filename_required);
                mNewFilename.setError(getString(R.string.alert_new_filename_required));
                return;
            }

            if (newFilename.contains("/")) {
                mViewUtils.showToastMessage(R.string.alert_filename_format);
                mNewFilename.setError(getString(R.string.alert_filename_format));
                return;
            }

            File file = new File(getRootFolder(), newFilename);
            if (file.exists()) {
                mViewUtils.showToastMessage(R.string.alert_file_exists);
                mNewFilename.setError(getString(R.string.alert_file_exists));
                return;
            }
            mFile.renameTo(file);
            refreshList();
            dismiss();
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            FsUtils.getInstance(PrivateKeyManageActivity.this).deleteFile(mFile);
            refreshList();
        }

    }

    private void refreshList() {
        setCurrentDir(getRootFolder());
        RepoUtils.getInstance(this).refreshSgitTransportCallback();
    }

}
