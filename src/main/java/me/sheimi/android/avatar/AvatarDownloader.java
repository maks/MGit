package me.sheimi.android.avatar;

import android.content.Context;
import android.content.SharedPreferences;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import me.sheimi.sgit.R;

/**
 * Created by cfoote on 12/06/2017.
 *
 * Custom image downloader to support an 'avatar' scheme.
 */
public class AvatarDownloader extends BaseImageDownloader {

    private static final String IMAGE_REQUEST_HASH = "http://www.gravatar.com/avatar/%s?s=40&d=identicon";
    private static final String AVATAR_SCHEME = "avatar://";

    private boolean useGravatar;

    public AvatarDownloader(Context context) {
        super(context);
        useGravatar = isGravatarEnabled();
    }

    public AvatarDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
        useGravatar = isGravatarEnabled();
    }

    /**
     * Checks if the use of Gravatar is enabled in the preferences.
     * @return true if the use of Gravatar to retrieve Avatar images is enabled, false otherwise
     */
    protected boolean isGravatarEnabled() {
        SharedPreferences sharedPreference = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        return sharedPreference.getBoolean(context.getString(R.string.pref_key_use_gravatar), true);
    }

    /**
     * Retrieves {@link InputStream} of image by URI that uses the avatar:// scheme.<br />
     * Throws {@link UnsupportedOperationException} if the image URI has an unsupported scheme.
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException                   if some I/O error occurs
     * @throws UnsupportedOperationException if image URI has unsupported scheme(protocol)
     */
    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (imageUri.toLowerCase(Locale.US).startsWith(AVATAR_SCHEME)) {
            if (!useGravatar)
                return null;

            String hash = imageUri.substring(AVATAR_SCHEME.length());
            String gravatarUri = String.format(Locale.getDefault(), IMAGE_REQUEST_HASH, hash);
            return getStream(gravatarUri, extra);
        }

        return super.getStreamFromOtherSource(imageUri, extra);
    }
}
