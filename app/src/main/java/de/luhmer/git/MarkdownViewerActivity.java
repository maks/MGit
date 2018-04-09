package de.luhmer.git;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
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
import java.util.Stack;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;

import static android.text.Html.escapeHtml;

public class MarkdownViewerActivity extends SheimiFragmentActivity {

    private WebView mMarkdownView;

    public final static String FILE_NAME = "FILENAME";
    private String fileBasePath;
    private Stack<String> urlStack = new Stack<>();
    private String currentlyOpenedFile;


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
        currentlyOpenedFile = filename;

        try {
            // TODO handle filenames with # at the end.. Sample.md#test

            final String content = getStringFromFile(fileBasePath + filename);
            urlStack.push(filename);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loadMarkdownToView(content, "file:///android_asset/markdown-viewer/markdown-css/alt.css");
                    //loadMarkdownToView(content, "file:///android_asset/markdown-viewer/markdown-css/classic.css");
                    //loadMarkdownToView(content, "file:///android_asset/markdown-viewer/markdown-css/foghorn.css");
                    loadMarkdownToView(content, "file:///android_asset/markdown-viewer/markdown-css/paperwhite.css");
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

        mMarkdownView.addJavascriptInterface(new JsMarkdownInterface(this), "android");


        WebViewClient wvc = new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    urlStack.add(request.getUrl().toString());
                }
                return super.shouldOverrideUrlLoading(view, request);
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

        Context mContext;

        /** Instantiate the interface and set the context */
        JsMarkdownInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void openLink(String link) {
            //Toast.makeText(mContext, link, Toast.LENGTH_SHORT).show();
            loadMarkdownToView(link);
        }

        @JavascriptInterface
        public void log(String log) {
            Log.d(TAG, log);
        }
    }
}
