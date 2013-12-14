package me.sheimi.sgit.activities.explorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileFilter;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.utils.ActivityUtils;

public class ImportRepositoryActivity extends FileExplorerActivity {

    @Override
    protected File getRootFolder() {
        return Environment.getExternalStorageDirectory();
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (!file.isDirectory())
                    return false;
                return true;
            }
        };
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                    int position, long id) {
                final File file = mFilesListAdapter.getItem(position);
                File dotGit = new File(file, Repo.DOT_GIT_DIR);
                if (!dotGit.exists()) {
                    setCurrentDir(file);
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ImportRepositoryActivity.this);
                builder.setTitle(R.string.dialog_comfirm_import_repo_title);
                builder.setMessage(R.string.dialog_comfirm_import_repo_msg);
                builder.setNegativeButton(R.string.label_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                setCurrentDir(file);
                            }
                        });
                builder.setPositiveButton(R.string.label_import,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.putExtra(RESULT_PATH,
                                        file.getAbsolutePath());
                                setResult(Activity.RESULT_OK, intent);
                                ActivityUtils
                                        .finishActivity(ImportRepositoryActivity.this);
                            }
                        });
                builder.show();
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return null;
    }
}
