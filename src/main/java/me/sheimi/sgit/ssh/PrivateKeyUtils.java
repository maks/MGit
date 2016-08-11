package me.sheimi.sgit.ssh;

import me.sheimi.android.utils.FsUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PrivateKeyUtils {
    private PrivateKeyUtils() {}

    public static File getPrivateKeyFolder() {
        return FsUtils.getInternalDir("ssh");
    }

    public static void migratePrivateKeys() {
        File oldDir = FsUtils.getExternalDir("ssh");
        if (oldDir.exists()) {
            try {
                FileUtils.copyDirectory(oldDir, getPrivateKeyFolder());
                FileUtils.deleteDirectory(oldDir);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
