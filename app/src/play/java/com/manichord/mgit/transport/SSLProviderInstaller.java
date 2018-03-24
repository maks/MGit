package com.manichord.mgit.transport;

import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import timber.log.Timber;

/**
 * Created by kaeptmblaubaer1000 on 23.03.2018.
 * <p>
 * This class is a wrapper for {@link com.google.android.gms.security.ProviderInstaller}.
 * <p>
 * There is another implementation using Conscrypt.
 */

public class SSLProviderInstaller {
    public static void install(Context ctx) {
        try {
            ProviderInstaller.installIfNeeded(ctx);
        } catch (GooglePlayServicesRepairableException e) {
            showGooglePlayError(ctx, e);
        } catch (GooglePlayServicesNotAvailableException e) {
            showGooglePlayError(ctx, e);
        }
    }

    private static void showGooglePlayError(Context ctx, Exception e) {
        Timber.e(e);
        SheimiFragmentActivity.showMessageDialog(ctx, R.string.error_need_play_services_title,
            ctx.getString(R.string.error_need_play_services_message));
    }
}
