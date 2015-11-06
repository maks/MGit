package me.sheimi.sgit.activities;

import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.CodeGuesser;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.CommitDiffTask;
import me.sheimi.sgit.repo.tasks.repo.CommitDiffTask.CommitDiffResult;

import org.eclipse.jgit.diff.DiffEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.lib.PersonIdent;

public class CommitDiffActivity extends SheimiFragmentActivity {

    public final static String OLD_COMMIT = "old commit";
    public final static String NEW_COMMIT = "new commit";
    public final static String SHOW_DESCRIPTION = "show_description";
    private static final String JS_INF = "CodeLoader";
    private WebView mDiffContent;
    private ProgressBar mLoading;
    private String mOldCommit;
    private String mNewCommit;
    private boolean mShowDescription;
    private Repo mRepo;
    private RevCommit mCommit;

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
	    mShowDescription = extras.getBoolean(SHOW_DESCRIPTION);
        mRepo = (Repo) extras.getSerializable(Repo.TAG);

        String title = Repo.getCommitDisplayName(mNewCommit) + " : "
                + Repo.getCommitDisplayName(mOldCommit);

        setTitle(getString(R.string.title_activity_commit_diff) + title);
        loadFileContent();
    }

    private void loadFileContent() {
        mDiffContent.addJavascriptInterface(new CodeLoader(), JS_INF);
        mDiffContent.loadDataWithBaseURL("file:///android_asset/", HTML_TMPL,
                "text/html", "utf-8", null);
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
        public boolean haveCommitInfo() {
            return (mCommit != null);
        }

        @JavascriptInterface
        public String getCommitInfo() {
            if (mCommit == null) {
                return "";
            }

            PersonIdent committer, author;
            String ret;
            committer = mCommit.getCommitterIdent();
            author = mCommit.getAuthorIdent();
            ret = "commit " + mNewCommit + "\n"
                    + "Author:     " + author.getName() + " <" + author.getEmailAddress() + ">\n"
                    + "AuthorDate: " + author.getWhen() + "\n"
                    + "Commit:     " + committer.getName() + " <" + committer.getEmailAddress() + "\n"
                    + "CommitDate: " + committer.getWhen() + "\n";
            return ret;
        }


        @JavascriptInterface
        public String getCommitMessage() {
            if (mCommit == null) {
                return "";
            }
            return mCommit.getFullMessage();
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
            CommitDiffTask diffTask = new CommitDiffTask(mRepo, mOldCommit,
                    mNewCommit, new CommitDiffResult() {
                @Override
                public void pushResult(List<DiffEntry> diffEntries,
                                       List<String> diffStrs, RevCommit commit) {
                    mDiffEntries = diffEntries;
                    mDiffStrs = diffStrs;
                    mCommit = commit;
                    mLoading.setVisibility(View.GONE);
                    mDiffContent.loadUrl(CodeGuesser.wrapUrlScript("notifyEntriesReady();"));
                }
            }, mShowDescription);
            diffTask.executeTask();
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
