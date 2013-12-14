package me.sheimi.sgit.utils.ssh;

import android.content.Context;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

/**
 * Created by sheimi on 8/22/13.
 */
public class SgitTransportCallback implements TransportConfigCallback {

    private SGitSessionFactory ssh;

    public SgitTransportCallback(Context context) {
        ssh = new SGitSessionFactory(context);
    }

    @Override
    public void configure(Transport tn) {
        if (tn instanceof SshTransport) {
            ((SshTransport) tn).setSshSessionFactory(ssh);
        }
    }
}