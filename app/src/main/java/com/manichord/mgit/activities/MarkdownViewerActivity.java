package com.manichord.mgit.activities;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import me.sheimi.android.activities.SheimiFragmentActivity;

import static android.text.Html.escapeHtml;

public class MarkdownViewerActivity extends SheimiFragmentActivity {

    public final static String FILE_NAME = "FILENAME";
    private static final Map<String, String> cssStyles;
    static {
        Map<String, String> cStyles = new HashMap<>();
        cStyles.put("alt", "file:///android_asset/markdown-viewer/markdown-css/alt.css");
        cStyles.put("classic", "file:///android_asset/markdown-viewer/markdown-css/classic.css");
        cStyles.put("foghorn", "file:///android_asset/markdown-viewer/markdown-css/foghorn.css");
        cStyles.put("paperwhite", "file:///android_asset/markdown-viewer/markdown-css/paperwhite.css");
        cssStyles = Collections.unmodifiableMap(cStyles);
    }

    private WebView mMarkdownView;

    private String fileBasePath;
    private Stack<String> urlStack = new Stack<>();
    private String selectedTheme = "paperwhite"; // TODO export theme as app setting


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMarkdownView = new WebView(this) { };
        setContentView(mMarkdownView);
        init();

        String fullPath = getIntent().getStringExtra(FILE_NAME);
        int fileNameStart = fullPath.lastIndexOf("/")+1;
        fileBasePath = fullPath.substring(0, fileNameStart);
        String filename = fullPath.substring(fileNameStart);

        loadMarkdownToView(filename);
    }

    private void loadMarkdownToView(String filename) {
        try {
            // TODO handle filenames with # at the end.. Sample.md#test

            final String content = getStringFromFile(fileBasePath + filename);
            urlStack.push(filename);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadMarkdownToView(content, cssStyles.get(selectedTheme));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadMarkdownToView(String txt, String cssFileUrl) {
        String html = null;
        try {
            html = getMarkDownMathJaxTemplate();

            if (cssFileUrl != null) {
                html = html.replace("STYLESHEET_GOES_HERE", cssFileUrl);
                html = html.replace("MARKDOWN_GOES_HERE", escapeHtml(txt.replace("\r\n", "<br>")));
                html = html.replace("src=\"marked.js\"", "src=\"file:///android_asset/markdown-viewer/js/marked.js\"");
                html = html.replace("FILE_BASE_PATH", fileBasePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMarkdownView.loadDataWithBaseURL("fake://", html, "text/html", "UTF-8", null);
    }


    private String getMarkDownMathJaxTemplate() throws IOException {
        AssetManager am = getAssets();
        InputStream is = am.open("markdown-viewer/markdown_template.html");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine + "\n");
            mLine = reader.readLine();
        }
        reader.close();
        is.close();

        return sb.toString();
    }

    private void init() {
        WebSettings webSettings = mMarkdownView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        mMarkdownView.addJavascriptInterface(new JsMarkdownInterface(), "android");


        WebViewClient wvc = new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                urlStack.add(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        };
        mMarkdownView.setWebViewClient(wvc);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(urlStack.size() >= 2) {
                    urlStack.pop(); // Pop current website url
                    String url = urlStack.pop(); // get previous url
                    if(url.startsWith("http")) {
                        urlStack.push(url);
                        mMarkdownView.loadUrl(url);
                    } else {
                        loadMarkdownToView(url);
                    }
                } else {
                    finish();
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    public class JsMarkdownInterface {
        private static final String TAG = "JsMarkdownInterface";

        /** Show a toast from the web page */
        @JavascriptInterface
        public void openLink(String link) {
            loadMarkdownToView(link);
        }

        @JavascriptInterface
        public void log(String log) {
            Log.d(TAG, log);
        }
    }
}
