package me.sheimi.sgit.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.umeng.analytics.MobclickAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import me.sheimi.sgit.R;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.CodeUtils;

public class ViewFileActivity extends Activity {

    public static String TAG_FILE_NAME = "file_name";
    private WebView mFileContent;
    private static final String JS_INF = "CodeLoader";
    private String mCode;
    private int mCodeLines;
    private String mCodeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        setupActionBar();
        mFileContent = (WebView) findViewById(R.id.fileContent);

        Bundle extras = getIntent().getExtras();
        String fileName = extras.getString(TAG_FILE_NAME);
        File file = new File(fileName);
        setTitle(getString(R.string.view_file_title) + file.getName());
        mCodeType = CodeUtils.guessCodeType(file.getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_file, menu);
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

        @JavascriptInterface
        public String getCode() {
            return mCode;
        }

        @JavascriptInterface
        public int getLineNumber() {
            return mCodeLines;
        }

        @JavascriptInterface
        public String getCodeType() {
            return mCodeType;
        }

    }

    private static final String HTML_TMPL = "<!doctype html>"
            + "<head>"
            + " <script src=\"js/jquery.js\"></script>"
            + " <script src=\"js/highlight.pack.js\"></script>"
            + " <script src=\"js/local.js\"></script>"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/tne.css\" />"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/local.css\" />"
            + "</head><body><table>"
            + "<tbody><tr><td class=\"line_number_td\">"
            + "<pre class=\"line_numbers\"></pre>"
            + "</td><td class=\"codes_td\"><pre class=\"codes\"><code></code></pre>"
            + "</td></tr></tbody></table></body>";

}
