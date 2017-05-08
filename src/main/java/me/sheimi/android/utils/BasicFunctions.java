package me.sheimi.android.utils;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by sheimi on 8/19/13.
 */
public class BasicFunctions {

    private static final String IMAGE_REQUEST_HASH = "http://www.gravatar.com/avatar/%s?s=40&d=identicon";

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; ++i) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            BasicFunctions.showException(e);
        }
        return "";
    }

    public static String buildGravatarURL(String email) {
        String hash = md5(email);
        String url = String.format(Locale.getDefault(), IMAGE_REQUEST_HASH,
                hash);
        return url;
    }

    private static SheimiFragmentActivity mActiveActivity;

    public static SheimiFragmentActivity getActiveActivity() {
        return mActiveActivity;
    }

    public static void setActiveActivity(SheimiFragmentActivity activity) {
        mActiveActivity = activity;
    }

    public static ImageLoader getImageLoader() {
        return getActiveActivity().getImageLoader();
    }

    public static void showException(Throwable t) {
        SheimiFragmentActivity activity = BasicFunctions.getActiveActivity();
        activity.showToastMessage(t.getMessage());
        t.printStackTrace();
    }

    public static void showException(Throwable t, int res) {
        SheimiFragmentActivity activity = BasicFunctions.getActiveActivity();
        StackTraceElement[] ste = t.getStackTrace();
        String str = (t.getCause() != null) ? t.getCause().getMessage()+"\n" : "\n";
        for (int i=0; i < ste.length; i++){
            str += ste[i].toString()+"\n";
        }
        final String str2 = str;
        activity.showMessageDialog(R.string.dialog_show_invalid_remote, str2, R.string.action_send_report, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{getActiveActivity().getString(R.string.report_mail)});
                intent.putExtra(Intent.EXTRA_SUBJECT, getActiveActivity().getString(R.string.dialog_show_invalid_remote));
                intent.putExtra(Intent.EXTRA_TEXT, str2);
                try {
                    getActiveActivity().startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActiveActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
