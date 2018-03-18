package me.sheimi.sgit.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import java.io.File;

import me.sheimi.sgit.SGitApplication;
import timber.log.Timber;

/**
 * Custom config for Jsch, including using user-provided private keys
 */
public class SGitSessionFactory extends JschConfigSessionFactory {

    @Override
    protected void configure(Host host, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,password");

        // Awful use of App singleton but not really any other way to get hold of a provider that needs
        // to have been initialised with an Android context
        UserInfo userInfo = new CredentialsProviderUserInfo(session, SGitApplication.getJschCredentialsProvider());
        session.setUserInfo(userInfo);
    }


    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jsch = new JSch();
        PrivateKeyUtils.migratePrivateKeys();
        File sshDir = PrivateKeyUtils.getPrivateKeyFolder();
        for (File file : sshDir.listFiles()) {
            KeyPair kpair = KeyPair.load(jsch, file.getAbsolutePath());
            jsch.addIdentity(file.getAbsolutePath());
        }
        return jsch;
    }

}
