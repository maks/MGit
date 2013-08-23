package me.sheimi.sgit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.ads.AdView;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.AdUtils;

public class ExploreFileActivity extends Activity {

    public static final String RESULT_PATH = "result_path";

    private File mSDCardFolder;
    private File mCurrentDir;
    private ListView mFileList;
    private FilesListAdapter mFilesListAdapter;
    private AdUtils mAdUtils;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        mSDCardFolder = Environment.getExternalStorageDirectory();
        mCurrentDir = mSDCardFolder;
        mFileList = (ListView) findViewById(R.id.fileList);
        mFilesListAdapter = new FilesListAdapter(this);
        mFileList.setAdapter(mFilesListAdapter);
        mFilesListAdapter.setDir(mSDCardFolder);

        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (file.isDirectory()) {
                    mCurrentDir = file;
                    mFilesListAdapter.setDir(mCurrentDir);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(RESULT_PATH, file.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                ActivityUtils.finishActivity(ExploreFileActivity.this);
            }
        });

        mAdUtils = AdUtils.getInstance(this);
        mAdView = (AdView) findViewById(R.id.adView);
        mAdUtils.loadAds(mAdView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtils.finishActivity(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.explore_file, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mSDCardFolder.equals(mCurrentDir)) {
                mCurrentDir = mCurrentDir.getParentFile();
                mFilesListAdapter.setDir(mCurrentDir);
                return true;
            }
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

}
