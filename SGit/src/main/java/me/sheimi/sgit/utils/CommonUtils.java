package me.sheimi.sgit.utils;

import android.app.Activity;
import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import me.sheimi.sgit.utils.ssh.SgitTransportCallback;

/**
 * Created by sheimi on 8/19/13.
 */
public class CommonUtils {

    private static final String IMAGE_REQUEST_HASH =
            "http://www.gravatar.com/avatar/%s?s=40";

    private SgitTransportCallback mSgitTransportCallback;

    private Context mContext;
    private static CommonUtils mInstance;

    private CommonUtils(Context context) {
        mContext = context;
        refreshSgitTransportCallback();
    }

    public static CommonUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CommonUtils(context);
        }
        if (context != null) {
            mInstance.mContext = context;
        }
        return mInstance;
    }

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String buildGravatarURL(String email) {
        String hash = md5(email);
        String url = String.format(Locale.getDefault(), IMAGE_REQUEST_HASH,
                hash);
        return url;
    }

    public static boolean isDebug(Activity activity) {
        return false;
    }


    public void refreshSgitTransportCallback() {
        mSgitTransportCallback = new SgitTransportCallback(mContext);
    }

    public SgitTransportCallback getSgitTransportCallback() {
        return mSgitTransportCallback;
    }
}
