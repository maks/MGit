package me.sheimi.sgit.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sheimi on 8/8/13.
 */
public class FsUtils {

    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    public static final String PNG_SUFFIX = ".png";
    public static final String TEMP_DIR = "temp";
    public static final String REPO_DIR = "repo";

    private static FsUtils mInstance;

    private Context mContext;

    private FsUtils(Context context) {
        mContext = context;
    }

    public static FsUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FsUtils(context);
        }
        return mInstance;
    }

    public File createTempFile(String subfix) {
        File dir = getDir(TEMP_DIR);
        String fileName = TIMESTAMP_FORMATTER.format(new Date());
        File file = null;
        try {
            file = File.createTempFile(fileName, subfix, dir);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public File getDir(String dirname) {
        File mDir = new File(getAppDir(), dirname);
        if (mDir.exists()) {
            mDir.mkdir();
        }
        return mDir;
    }

    public File getRepo(String localPath) {
        return getDir(REPO_DIR + "/" + localPath);
    }

    public File getAppDir() {
        return mContext.getExternalFilesDir(null);
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url
                .toLowerCase(Locale.getDefault()));
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        if (type == null) {
            type = "text/plain";
        }
        return type;
    }

    public String getMimeType(File file) {
        return getMimeType(Uri.fromFile(file).toString());
    }

    public void openFile(File file) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        String type = getMimeType(uri.toString());
        intent.setDataAndType(uri, type);
        mContext.startActivity(intent);
    }

    public void deleteFile(File file) {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        for (File f : file.listFiles()) {
            deleteFile(f);
        }
        file.delete();
    }

}
