package me.sheimi.sgit.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.umeng.analytics.MobclickAgent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.ArrayList;
import java.util.List;

import me.sheimi.sgit.R;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.RepoUtils;

public class CommitDiffActivity extends FragmentActivity {

    public final static String OLD_COMMIT = "old commit";
    public final static String NEW_COMMIT = "new commit";
    public final static String LOCAL_REPO = "local repo";
    private static final String JS_INF = "CodeLoader";
    private WebView mDiffContent;
    private RepoUtils mRepoUtils;
    private String mLocalRepo;
    private String mOldCommit;
    private String mNewCommit;
    private Git mGit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        setupActionBar();
        mDiffContent = (WebView) findViewById(R.id.fileContent);
        mRepoUtils = RepoUtils.getInstance(this);

        Bundle extras = getIntent().getExtras();
        mOldCommit = extras.getString(OLD_COMMIT);
        mNewCommit = extras.getString(NEW_COMMIT);
        mLocalRepo = extras.getString(LOCAL_REPO);
        mGit = mRepoUtils.getGit(mLocalRepo);

        String title = mRepoUtils.getCommitDisplayName(mNewCommit) + " : "
                + mRepoUtils.getCommitDisplayName(mOldCommit);

        setTitle(getString(R.string.title_activity_commit_diff) + title);

        loadFileContent();
    }

    private void loadFileContent() {
        mDiffContent.loadDataWithBaseURL("file:///android_asset/", HTML_TMPL,
                "text/html", "utf-8", null);
        mDiffContent.addJavascriptInterface(new CodeLoader(), JS_INF);
        WebSettings webSettings = mDiffContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mDiffContent.setWebChromeClient(new WebChromeClient() {
            public void onConsoleMessage(String message, int lineNumber,
                                         String sourceID) {
                Log.d("MyApplication", message + " -- From line " +
                        lineNumber
                        + " of " + sourceID);
            }

            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url) {
                return false;
            }
        });
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.diff_commits, menu);
        return true;
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
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

    private class CodeLoader {

        private List<String> mDiffStrs;
        private List<DiffEntry> mDiffEntries;

        @JavascriptInterface
        public String getDiff(int index) {
            return mDiffStrs.get(index);
        }

        @JavascriptInterface
        public void getDiffEntries() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mDiffEntries = mRepoUtils.getCommitDiff(mGit, mOldCommit, mNewCommit);
                    mDiffStrs = new ArrayList<String>(mDiffEntries.size());
                    for (DiffEntry diffEntry : mDiffEntries) {
                        String diffStr = mRepoUtils.parseDiffEntry(mGit, diffEntry);
                        mDiffStrs.add(diffStr);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDiffContent.loadUrl(wrapUrlScript("notifyEntriesReady();"));
                        }
                    });
                }
            });
            thread.start();
        }

        @JavascriptInterface
        public int getDiffSize() {
            return mDiffEntries.size();
        }

    }

    private static String wrapUrlScript(String script) {
        return String.format(URL_SCRIPT_WRAPPER, script);
    }

    private final static String URL_SCRIPT_WRAPPER = "javascript:(function(){%s;})()";

    private static final String HTML_TMPL = "<!doctype html>"
            + "<head>"
            + " <script src=\"js/jquery.js\"></script>"
            + " <script src=\"js/highlight.pack.js\"></script>"
            + " <script src=\"js/local_commits_diff.js\"></script>"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/tne.css\" />"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/local_commits_diff.css\" />"
            + "</head><body></body>";

}
