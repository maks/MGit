package me.sheimi.sgit.activities.explorer;

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;

public class ExploreFileActivity extends FileExplorerActivity {

    @Override
    protected File getRootFolder() {
        return Environment.getExternalStorageDirectory();
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                String filename = file.getName();
                return !filename.startsWith(".");
            }
        };
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                    int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (file.isDirectory()) {
                    setCurrentDir(file);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(RESULT_PATH, file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return null;
    }

}
