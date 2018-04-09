package com.manichord.mgit.transport;

import android.content.Context;

import org.conscrypt.Conscrypt;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import timber.log.Timber;

/**
 * Created by kaeptmblaubaer1000 on 23.03.2018.
 * <p>
 * This class is an installer for {@link org.conscrypt.OpenSSLProvider}.
 */

public class SSLProviderInstaller {
    private static Provider provider = null;

    // The other implementation uses the param.
    public static synchronized void install(@SuppressWarnings("unused") Context ctx) {
        if (provider == null) {
            provider = Conscrypt.newProvider("MGit_OpenSSL");
        }
        try {
            final int pos = Security.insertProviderAt(provider, 1);
            switch (pos) {
                case 1:
                    SSLContext sslContext = SSLContext.getInstance("Default");
                    //noinspection JavaReflectionMemberAccess
                    Field field = SSLSocketFactory.class.getDeclaredField("defaultSocketFactory");
                    field.setAccessible(true);
                    field.set(null, sslContext.getSocketFactory());
                    //noinspection JavaReflectionMemberAccess
                    field = SSLServerSocketFactory.class.getDeclaredField("defaultServerSocketFactory");
                    field.setAccessible(true);
                    field.set(null, sslContext.getServerSocketFactory());
                    Security.setProperty("ssl.SocketFactory.provider", "org.conscrypt.OpenSSLSocketFactoryImpl");
                    Security.setProperty("ssl.ServerSocketFactory.provider", "org.conscrypt.OpenSSLServerSocketFactoryImpl");
                    SSLContext.setDefault(sslContext);
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                    Timber.i("Installed default security provider MGit_OpenSSL");

                    // This fallthrough is for the above case a break, and since we want to exclude -1 this is great.
                case -1:
                    break;
                default:
                    String str = "Failed to install security provider MGit_OpenSSL, result: ";
                    Timber.e(str + pos);
                    throw new SecurityException();
            }

        } catch (NoSuchAlgorithmException e) {
            Timber.e("Failed to find SSLContext.Default provider");
            throw new SecurityException();
        } catch (IllegalAccessException e) {
            Timber.e("Failed to set socket factory via reflection");
            throw new SecurityException();
        } catch (NoSuchFieldException e) {
            Timber.e("Failed to set socket factory via reflection");
            throw new SecurityException();
        } catch (UnsatisfiedLinkError e) {
            throw new SecurityException();
        }
    }
}
