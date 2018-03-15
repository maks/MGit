package com.manichord.mgit.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class MGitSSLSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory wrappedSSLSocketFactory;

    public MGitSSLSocketFactory(SSLSocketFactory wrapped) {
        wrappedSSLSocketFactory = wrapped;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return wrappedSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return wrappedSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return wrappedSSLSocketFactory.createSocket();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return wrappedSSLSocketFactory.createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return wrappedSSLSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return wrappedSSLSocketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return wrappedSSLSocketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return wrappedSSLSocketFactory.createSocket(address, port, localAddress, localPort);
    }
}
