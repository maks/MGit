package me.sheimi.sgit.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import me.sheimi.sgit.R;
import me.sheimi.sgit.utils.ActivityUtils;

public class ViewFileActivity extends Activity {

    public static String TAG_FILE_NAME = "file_name";
    private WebView mFileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        setupActionBar();
        mFileContent = (WebView) findViewById(R.id.fileContent);

        Bundle extras = getIntent().getExtras();
        String fileName = extras.getString(TAG_FILE_NAME);
        File file = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (null != line) {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            String text = String.format(Locale.getDefault(), HTML_TMPL, sb);
            mFileContent.loadDataWithBaseURL("file:///android_asset/", text,
                    "text/html", "utf-8", null);
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtils.finishActivity(this);
            return true;
        }
        return false;
    }

    private static final String HTML_TMPL = "<!doctype html>"
            + "<head>"
            + " <script src=\"js/jquery.js\"></script>"
            + " <script src=\"js/highlight.pack.js\"></script>"
            + " <link type=\"text/css\" rel=\"stylesheet\" href=\"css/xcode" +
            ".css\" />"
            + "</head>"
            + "<body>"
            + " <pre><code>%s</code></pre>"
            + " <script>"
            + "  var code = $('pre code').html();"
            + "  $('pre code').text(code);"
            + "  hljs.initHighlightingOnLoad();"
            + " </script>"
            + "</body>";

}
