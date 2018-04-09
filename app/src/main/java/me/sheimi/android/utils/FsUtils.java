package me.sheimi.android.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;

import org.apache.commons.io.FileUtils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.MimeTypeMap;

/**
 * Created by sheimi on 8/8/13.
 */
public class FsUtils {

    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault());
    public static final String TEMP_DIR = "temp";
    private static final String LOGTAG = FsUtils.class.getSimpleName();

    private FsUtils() {
    }

    public static File createTempFile(String subfix) throws IOException {
        File dir = getExternalDir(TEMP_DIR);
        String fileName = TIMESTAMP_FORMATTER.format(new Date());
        File file = File.createTempFile(fileName, subfix, dir);
        file.deleteOnExit();
        return file;
    }

    /**
     * Get a File representing a dir within the external shared location where files can be stored specific to this app
     * creating the dir if it doesn't already exist
     *
     * @param dirname
     * @return
     */
    public static File getExternalDir(String dirname) {
        return getExternalDir(dirname, true);
    }

    /**
     *
     * @param dirname
     * @return
     */
    public static File getInternalDir(String dirname) { return getExternalDir(dirname, true, false); }

    /**
     * Get a File representing a dir within the external shared location where files can be stored specific to this app
     *
     * @param dirname
     * @param isCreate  create the dir if it does not already exist
     * @return
     */
    public static File getExternalDir(String dirname, boolean isCreate) { return getExternalDir(dirname, isCreate, true); }

    /**
     *
     * Get a File representing a dir within the location where files can be stored specific to this app
     *
     * @param dirname  name of the dir to return
     * @param isCreate  create the dir if it does not already exist
     * @param isExternal if true, will use external *shared* storage
     * @return
     */
    public static File getExternalDir(String dirname, boolean isCreate, boolean isExternal) {
        File mDir = new File(getAppDir(isExternal), dirname);
        if (!mDir.exists() && isCreate) {
            mDir.mkdir();
        }
        return mDir;
    }

    /**
     * Get a File representing the location where files can be stored specific to this app
     *
     * @param isExternal if true, will use external *shared* storage
     * @return
     */
    public static File getAppDir(boolean isExternal) {
        SheimiFragmentActivity activeActivity = BasicFunctions.getActiveActivity();
        if (isExternal) {
            return activeActivity.getExternalFilesDir(null);
        } else {
            return activeActivity.getFilesDir();
        }
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
        BasicFunctions.getActiveActivity().startActivity(
                Intent.createChooser(
                        intent,
                        BasicFunctions.getActiveActivity().getString(
                                R.string.label_choose_app_to_open)));
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
            //TODO 
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

    public static boolean renameDirectory(File dir, String name) {
        String newDirPath = dir.getParent() + File.separator + name;
        File newDirFile = new File(newDirPath);

        return dir.renameTo(newDirFile);
    }

    public static String getRelativePath(File file, File base) {
        return base.toURI().relativize(file.toURI()).getPath();
    }

    public static File joinPath(File dir, String relative_path) {
        return new File(dir.getAbsolutePath() + File.separator + relative_path);
    }

}
