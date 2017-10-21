package me.sheimi.sgit.ssh;

import me.sheimi.android.utils.FsUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

public class PrivateKeyUtils {
    private PrivateKeyUtils() {}

    public static File getPrivateKeyFolder() {
        return FsUtils.getInternalDir("ssh");
    }

    public static File getPublicKeyFolder() {
        return FsUtils.getInternalDir("sshpub");
    }

    public static File getPublicKey(File privateKey) {
	return new File(PrivateKeyUtils.getPublicKeyFolder(),
			privateKey.getName());
    }

    public static File getPublicKeyEnsure(File privateKey) {
	File publicKey = getPublicKey(privateKey);
	if (!publicKey.exists()) {
	    try {
		JSch jsch=new JSch();
		KeyPair kpair=KeyPair.load(jsch, privateKey.getAbsolutePath());
		kpair.writePublicKey(new FileOutputStream(publicKey), "mgit");
		kpair.dispose();
	    } catch (Exception e) {
		//TODO 
		e.printStackTrace();
	    }
	}
	return publicKey;
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
