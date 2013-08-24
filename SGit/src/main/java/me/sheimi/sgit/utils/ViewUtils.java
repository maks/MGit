package me.sheimi.sgit.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by sheimi on 8/22/13.
 */
public class ViewUtils {

    private static ViewUtils mViewUtils;

    Context mContext;

    private ViewUtils(Context context) {
        mContext = context;
    }

    public static ViewUtils getInstance(Context context) {
        if (mViewUtils == null) {
            mViewUtils = new ViewUtils(context);
        }
        return mViewUtils;
    }

    public void showToastMessage(final String msg) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void showToastMessage(int resId) {
        showToastMessage(mContext.getString(resId));
    }

    public int getColor(int resId) {
        return mContext.getResources().getColor(resId);
    }

}
