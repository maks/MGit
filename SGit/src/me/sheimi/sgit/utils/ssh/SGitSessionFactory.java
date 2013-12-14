package me.sheimi.sgit.utils.ssh;

import android.content.Context;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import java.io.File;

import me.sheimi.sgit.utils.FsUtils;

/**
 * Created by sheimi on 8/22/13.
 */
public class SGitSessionFactory extends JschConfigSessionFactory {

    private Context mContext;

    public SGitSessionFactory(Context context) {
        mContext = context;
    }

    @Override
    protected void configure(Host arg0, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jsch = new JSch();
        File sshDir = FsUtils.getInstance(mContext).getDir("ssh");
        for (File file : sshDir.listFiles()) {
            jsch.addIdentity(file.getAbsolutePath());
        }
        return jsch;
    }

}