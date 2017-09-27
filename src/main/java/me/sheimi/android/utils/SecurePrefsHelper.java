package me.sheimi.android.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;

import com.securepreferences.SecurePreferences;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import timber.log.Timber;

/**
 * Securely store sensitive data in prefs, encrypting with a key pair that is stored in
 * the Android KeyStore, thus min of API 18 (4.3) is required.
 *
 * The basic idea came from:
 * Ref: https://medium.com/@ali.muzaffar/securing-sharedpreferences-in-android-a21883a9cbf8
 *
 * while the actual code comes from:
 * Ref: https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3
 *
 * But in this class, all we do is use the generated RSA cert as the "password" used for AES encryption
 * by the SecurePreferences library, by taking the md5 of the RSA keys certificates toString()
 */

public class SecurePrefsHelper {

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALIAS = "mgit_prefs";
    private static final String SEC_PREFS_FILE_NAME = "sec_prefs.xml";
    private static final String KEY_ALGORITHM_RSA = "RSA"; //KeyProperties.KEY_ALGORITHM_RSA is only available in API 23, so need to define it here

    SharedPreferences mSecurePrefs;
    private KeyStore mKeyStore;

    public SecurePrefsHelper(Context context) throws SecurePrefsException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            throw new SecurePrefsException("Min Android version require:"+Build.VERSION_CODES.JELLY_BEAN_MR2);
        }

        try {
            mKeyStore = KeyStore.getInstance(AndroidKeyStore);
            mKeyStore.load(null);

            // make sure we have a keypair in keystore
            generateKeyPair(context);

            KeyStore.PrivateKeyEntry keypair = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(KEY_ALIAS, null);
            if (keypair == null) {
                throw new SecurePrefsException("missing keypair");
            }
            String prefsPassword = BasicFunctions.md5(keypair.getCertificate().toString());
            Timber.w("pref password %s", prefsPassword);
            mSecurePrefs = new SecurePreferences(context, prefsPassword, SEC_PREFS_FILE_NAME);

        } catch (KeyStoreException|CertificateException|NoSuchAlgorithmException|
            InvalidAlgorithmParameterException|NoSuchProviderException|IOException|UnrecoverableEntryException e) {
            Timber.e(e, "keystore error");
            throw new SecurePrefsException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void generateKeyPair(Context context) throws NoSuchProviderException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, KeyStoreException {
        // Generate the RSA key pairs
        if (!mKeyStore.containsAlias(KEY_ALIAS)) {
            // Generate a key pair for encryption
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, AndroidKeyStore);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }
    }

    /**
     * Retrieve a String value from the secured preferences.
     *
     * @param pref
     * @return value of pref or null if no such pref
     */
    public String get(String pref) {
        return mSecurePrefs.getString(pref, null);
    }

    /**
     * Store a String value into secured preferences.
     * @param name
     * @param value
     */
    public void set(String name, String value) {
        mSecurePrefs.edit().putString(name, value).apply();
    }
}
