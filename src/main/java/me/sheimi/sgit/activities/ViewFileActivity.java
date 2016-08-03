package me.sheimi.sgit.activities;

import java.io.File;
import java.io.IOException;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.utils.CodeGuesser;
import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.ChooseLanguageDialog;

import org.apache.commons.io.FileUtils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class ViewFileActivity extends SheimiFragmentActivity {

    public static String TAG_FILE_NAME = "file_name";
    private WebView mFileContent;
    private static final String JS_INF = "CodeLoader";
    private ProgressBar mLoading;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewFile();
    }

    private void initViewFile() {
        setContentView(R.layout.activity_view_file);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mFileContent = (WebView) findViewById(R.id.fileContent);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        Bundle extras = getIntent().getExtras();
        String fileName = extras.getString(TAG_FILE_NAME);
        mFile = new File(fileName);
        setTitle(mFile.getName());
        mFileContent.addJavascriptInterface(new CodeLoader(), JS_INF);
        WebSettings webSettings = mFileContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mFileContent.setWebChromeClient(new WebChromeClient() {
            public void onConsoleMessage(String message, int lineNumber,
                    String sourceID) {
                showToastMessage(message + " -- From line " + lineNumber
                        + " of " + sourceID);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFileContent();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadFileContent() {
        mFileContent.loadUrl("file:///android_asset/viewer.html");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri uri;
        String mimeType;
        Intent chooserIntent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_open_in_other_app:
                uri = Uri.fromFile(mFile);
                mimeType = FsUtils.getMimeType(uri.toString());
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(uri, mimeType);
                try {
                    chooserIntent = Intent.createChooser(viewIntent, 
                                getString(R.string.label_choose_app_to_open));
                    startActivity(chooserIntent);
                    forwardTransition();
                } catch (ActivityNotFoundException e) {
                    BasicFunctions.showException(e, R.string.error_no_open_app);
                } catch (Throwable e) {
                    BasicFunctions.showException(e);
                }
                break;
            case R.id.action_edit_in_other_app:
                uri = Uri.fromFile(mFile);
                mimeType = FsUtils.getMimeType(uri.toString());
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(uri, mimeType);
                try {
                    chooserIntent = Intent.createChooser(editIntent, 
                                getString(R.string.label_choose_app_to_edit));
                    startActivity(chooserIntent);
                    forwardTransition();
                } catch (ActivityNotFoundException e) {
                    BasicFunctions.showException(e, R.string.error_no_edit_app);
                } catch (Throwable e) {
                    BasicFunctions.showException(e);
                }
                break;
            case R.id.action_choose_language:
                ChooseLanguageDialog cld = new ChooseLanguageDialog();
                cld.show(getFragmentManager(), "choose language");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLanguage(String lang) {
        String js = String.format("setLang('%s')", lang);
        mFileContent.loadUrl(CodeGuesser.wrapUrlScript(js));
    }

    private class CodeLoader {

        private String mCode;

        @JavascriptInterface
        public String getCode() {
            return mCode;
        }

        @JavascriptInterface()
        public void loadCode() {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCode = FileUtils.readFileToString(mFile);
                    } catch (IOException e) {
                        BasicFunctions.showException(e);
                    }
                    display();
                }
            });
            thread.start();
        }

        private void display() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String lang = CodeGuesser.guessCodeType(mFile.getName());
                    String js = String.format("setLang('%s')", lang);
                    mFileContent.loadUrl(CodeGuesser.wrapUrlScript(js));
                    mLoading.setVisibility(View.INVISIBLE);
                    mFileContent.loadUrl(CodeGuesser
                            .wrapUrlScript("display()"));
                }
            });
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
