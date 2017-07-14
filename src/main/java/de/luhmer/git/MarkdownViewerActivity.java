package de.luhmer.git;

import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.markdownj.MarkdownProcessor;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import us.feras.mdv.MarkdownView;

public class MarkdownViewerActivity extends SheimiFragmentActivity {

    private WebView mMarkdownView;

    public final static String FILE_NAME = "FILENAME";
    private String fileBasePath;
    private Stack<String> urlStack = new Stack<>();
    private String currentlyOpenedFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markdown_viewer);


        mMarkdownView = new WebView(this) { };


        ConstraintLayout container = (ConstraintLayout) findViewById(R.id.contentContainer);
        container.addView(mMarkdownView);
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loadMarkdownToView(content, "file:///android_asset/markdown-css/alt.css");
                    //loadMarkdownToView(content, "file:///android_asset/markdown-css/classic.css");
                    //loadMarkdownToView(content, "file:///android_asset/markdown-css/foghorn.css");
                    loadMarkdownToView(content, "file:///android_asset/markdown-css/paperwhite.css");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadMarkdownToView(String txt, String cssFileUrl) {
        MarkdownProcessor m = new MarkdownProcessor();
        String content = m.markdown(txt);

        String html = "<!DOCTYPE html>\n<html>\n<head>";

        if (cssFileUrl != null) {
            html += "<link rel='stylesheet' type='text/css' href='"+ cssFileUrl +"' />";
        }

        html += "</head>\n<body>\n";
        html += content;
        html += "\n</body>\n</html>";

        // Images
        html = html.replaceAll("src=\"(.*?)\"", "src=\"file://" + fileBasePath + "/$1\"");

        // href's
        //html = html.replaceAll("href=\"(.*?)\"", "href=\"file://" + fileBasePath + "/$1\"");
        html = html.replaceAll("href=\"(.*?)\"", "onclick=\"android.openLink('$1')\" href=\"$1\"");

        mMarkdownView.loadDataWithBaseURL("fake://", html, "text/html", "UTF-8", null);
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
                return super.shouldOverrideUrlLoading(view, request);
            }
        };
        mMarkdownView.setWebViewClient(wvc);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(!urlStack.isEmpty()) {
                    loadMarkdownToView(urlStack.pop());
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

        Context mContext;

        /** Instantiate the interface and set the context */
        JsMarkdownInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void openLink(String link) {
            //Toast.makeText(mContext, link, Toast.LENGTH_SHORT).show();


            urlStack.add(currentlyOpenedFile);
            loadMarkdownToView(link);
        }
    }
}
