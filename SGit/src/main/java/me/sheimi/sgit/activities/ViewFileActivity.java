package me.sheimi.sgit.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.umeng.analytics.MobclickAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.ChooseLanguageDialog;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.CodeUtils;
import me.sheimi.sgit.utils.FsUtils;

public class ViewFileActivity extends SherlockFragmentActivity {

    public static String TAG_FILE_NAME = "file_name";
    private WebView mFileContent;
    private static final String JS_INF = "CodeLoader";
    private ProgressBar mLoading;
    private File mFile;
    private FsUtils mFsUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        setupActionBar();
        mFsUtils = FsUtils.getInstance(this);
        mFileContent = (WebView) findViewById(R.id.fileContent);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        Bundle extras = getIntent().getExtras();
        String fileName = extras.getString(TAG_FILE_NAME);
        mFile = new File(fileName);
        setTitle(mFile.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        loadFileContent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void loadFileContent() {
        mFileContent.loadDataWithBaseURL("file:///android_asset/", HTML_TMPL,
                "text/html", "utf-8", null);
        mFileContent.addJavascriptInterface(new CodeLoader(), JS_INF);
        WebSettings webSettings = mFileContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mFileContent.setWebChromeClient(new WebChromeClient() {
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.view_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtils.finishActivity(this);
                return true;
            case R.id.action_edit:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_EDIT);
                Uri uri = Uri.fromFile(mFile);
                String mimeType = mFsUtils.getMimeType(uri.toString());
                intent.setDataAndType(uri, mimeType);
                startActivity(intent);
                ActivityUtils.forwardTransition(this);
                return true;
            case R.id.action_choose_language:
                ChooseLanguageDialog cld = new ChooseLanguageDialog();
                cld.show(getSupportFragmentManager(), "choose language");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

    public void setLanguage(String lang) {
        String js = String.format("setLanguage('%s')", lang);
        mFileContent.loadUrl(CodeUtils.wrapUrlScript(js));
    }

    private class CodeLoader {

        private String mCode;
        private int mCodeLines;

        @JavascriptInterface
        public String getCode() {
            return mCode;
        }

        @JavascriptInterface
        public int getLineNumber() {
            return mCodeLines;
        }

        @JavascriptInterface
        public String getLanguage() {
            return CodeUtils.guessCodeType(mFile.getName());
        }

        @JavascriptInterface()
        public void loadCode() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(mFile));
                        StringBuffer sb = new StringBuffer();
                        String line = br.readLine();
                        mCodeLines = 0;
                        while (null != line) {
                            mCodeLines++;
                            sb.append(line);
                            sb.append('\n');
                            line = br.readLine();
                        }
                        mCode = sb.toString();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.INVISIBLE);
                            mFileContent.loadUrl(CodeUtils.wrapUrlScript("notifyFileLoaded();"));
                        }
                    });
                }
            });
            thread.start();
        }

    }

    private static final String HTML_TMPL = "<!doctype html>"
            + "<head>"
            + " <script src=\"js/jquery.js\"></script>"
            + " <script src=\"js/highlight.pack.js\"></script>"
            + " <script src=\"js/local_viewfile.js\"></script>"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/rainbow.css\" />"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/local_viewfile.css\" />"
            + "</head><body><table>"
            + "<tbody><tr><td class=\"line_number_td\">"
            + "<pre class=\"line_numbers\"></pre>"
            + "</td><td class=\"codes_td\"><pre class=\"codes\"><code></code></pre>"
            + "</td></tr></tbody></table></body>";

}
