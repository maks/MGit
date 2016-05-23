package me.sheimi.sgit.ssh;

import java.io.File;

import me.sheimi.android.utils.FsUtils;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Created by sheimi on 8/22/13.
 */
public class SGitSessionFactory extends JschConfigSessionFactory {

    @Override
    protected void configure(Host arg0, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jsch = new JSch();
        PrivateKeyUtils.migratePrivateKeys();
        File sshDir = PrivateKeyUtils.getPrivateKeyFolder();
        for (File file : sshDir.listFiles()) {
            jsch.addIdentity(file.getAbsolutePath());
        }
        return jsch;
    }

}