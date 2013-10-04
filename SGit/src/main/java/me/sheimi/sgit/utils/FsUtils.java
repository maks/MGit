package me.sheimi.sgit.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FileUtils;

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
        if (context != null) {
            mInstance.mContext = context;
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
        return getDir(dirname, true);
    }

    public File getDir(String dirname, boolean isCreate) {
        File mDir = new File(getAppDir(), dirname);
        if (!mDir.exists() && isCreate) {
            mDir.mkdir();
        }
        return mDir;
    }

    public File getRepo(String localPath) {
        return getDir(REPO_DIR + "/" + localPath, false);
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
        openFile(file, null);
    }

    public void openFile(File file, String mimeType) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        if (mimeType == null) {
            mimeType = getMimeType(uri.toString());
        }
        intent.setDataAndType(uri, mimeType);
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

    public void copyFile(File from, File to) {
        try {
            FileUtils.copyFile(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyDirectory(File from, File to) {
        if (!from.exists())
            return;
        try {
            FileUtils.copyDirectory(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
