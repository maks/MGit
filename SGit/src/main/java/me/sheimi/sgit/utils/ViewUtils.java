package me.sheimi.sgit.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Collection;

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

    public <T> void adapterAddAll(ArrayAdapter<T> adapter, Collection<T> collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            adapter.addAll(collection);
        } else {
            for (T item : collection) {
                adapter.add(item);
            }
        }
    }

    public <T> void adapterAddAll(ArrayAdapter<T> adapter, T[] collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            adapter.addAll(collection);
        } else {
            for (T item : collection) {
                adapter.add(item);
            }
        }
    }

}
