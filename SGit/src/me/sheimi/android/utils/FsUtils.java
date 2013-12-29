package me.sheimi.android.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.sheimi.sgit.R;

import org.apache.commons.io.FileUtils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/**
 * Created by sheimi on 8/8/13.
 */
public class FsUtils {

    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault());
    public static final String PNG_SUFFIX = ".png";
    public static final String TEMP_DIR = "temp";

    private FsUtils() {
    }

    public static File createTempFile(String subfix) {
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

    public static File getDir(String dirname) {
        return getDir(dirname, true);
    }

    public static File getDir(String dirname, boolean isCreate) {
        File mDir = new File(getAppDir(), dirname);
        if (!mDir.exists() && isCreate) {
            mDir.mkdir();
        }
        return mDir;
    }

    public static File getAppDir() {
        return BasicFunctions.getActiveActivity().getExternalFilesDir(null);
    }

    public static String getMimeType(String url) {
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

    public static String getMimeType(File file) {
        return getMimeType(Uri.fromFile(file).toString());
    }

    public static void openFile(File file) {
        openFile(file, null);
    }

    public static void openFile(File file, String mimeType) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        if (mimeType == null) {
            mimeType = getMimeType(uri.toString());
        }
        intent.setDataAndType(uri, mimeType);
        try {
            BasicFunctions.getActiveActivity().startActivity(
                    Intent.createChooser(
                            intent,
                            BasicFunctions.getActiveActivity().getString(
                                    R.string.label_choose_app_to_open)));
        } catch (ActivityNotFoundException e) {
            BasicFunctions.getActiveActivity().showToastMessage(
                    R.string.error_no_open_app);
        } catch (Throwable e) {
            BasicFunctions.getActiveActivity().showToastMessage(
                    R.string.error_can_not_open_file);
        }
    }

    public static void deleteFile(File file) {
        File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
        file.renameTo(to);
        deleteFileInner(to);
    }

    private static void deleteFileInner(File file) {
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void copyFile(File from, File to) {
        try {
            FileUtils.copyFile(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyDirectory(File from, File to) {
        if (!from.exists())
            return;
        try {
            FileUtils.copyDirectory(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRelativePath(File file, File base) {
        return base.toURI().relativize(file.toURI()).getPath();
    }

}
