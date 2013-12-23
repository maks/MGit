package me.sheimi.sgit.activities;

import java.util.ArrayList;
import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.CodeGuesser;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.diff.DiffEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CommitDiffActivity extends SheimiFragmentActivity {

    public final static String OLD_COMMIT = "old commit";
    public final static String NEW_COMMIT = "new commit";
    private static final String JS_INF = "CodeLoader";
    private WebView mDiffContent;
    private ProgressBar mLoading;
    private String mOldCommit;
    private String mNewCommit;
    private Repo mRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        setupActionBar();
        mDiffContent = (WebView) findViewById(R.id.fileContent);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        Bundle extras = getIntent().getExtras();
        mOldCommit = extras.getString(OLD_COMMIT);
        mNewCommit = extras.getString(NEW_COMMIT);
        mRepo = (Repo) extras.getSerializable(Repo.TAG);
        mRepo.setContext(this);

        String title = Repo.getCommitDisplayName(mNewCommit) + " : "
                + Repo.getCommitDisplayName(mOldCommit);

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
                Log.d("MyApplication", message + " -- From line " + lineNumber
                        + " of " + sourceID);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.diff_commits, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CodeLoader {

        private List<String> mDiffStrs;
        private List<DiffEntry> mDiffEntries;

        @JavascriptInterface
        public String getDiff(int index) {
            return mDiffStrs.get(index);
        }

        @JavascriptInterface
        public String getChangeType(int index) {
            DiffEntry diff = mDiffEntries.get(index);
            DiffEntry.ChangeType ct = diff.getChangeType();
            return ct.toString();
        }

        @JavascriptInterface
        public String getOldPath(int index) {
            DiffEntry diff = mDiffEntries.get(index);
            String op = diff.getOldPath();
            return op;
        }

        @JavascriptInterface
        public String getNewPath(int index) {
            DiffEntry diff = mDiffEntries.get(index);
            String np = diff.getNewPath();
            return np;
        }

        @JavascriptInterface
        public void getDiffEntries() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    mDiffEntries = mRepo.getCommitDiff(mOldCommit, mNewCommit);
                    mDiffStrs = new ArrayList<String>(mDiffEntries.size());
                    for (DiffEntry diffEntry : mDiffEntries) {
                        String diffStr = mRepo.parseDiffEntry(diffEntry);
                        mDiffStrs.add(diffStr);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.GONE);
                            mDiffContent.loadUrl(CodeGuesser
                                    .wrapUrlScript("notifyEntriesReady();"));
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

    private static final String HTML_TMPL = "<!doctype html>"
            + "<head>"
            + " <script src=\"js/jquery.js\"></script>"
            + " <script src=\"js/highlight.pack.js\"></script>"
            + " <script src=\"js/local_commits_diff.js\"></script>"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/rainbow.css\" />"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/local_commits_diff.css\" />"
            + "</head><body></body>";

}
