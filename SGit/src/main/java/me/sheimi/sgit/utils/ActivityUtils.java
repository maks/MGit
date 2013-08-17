package me.sheimi.sgit.utils;

import android.app.Activity;
import android.content.Intent;

import me.sheimi.sgit.R;

/**
 * Created by sheimi on 8/17/13.
 */
public class ActivityUtils {

    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        forwardTransition(activity);
    }

    public static void finishActivity(Activity activity) {
        activity.finish();
        backTransition(activity);
    }

    public static void forwardTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_left);
    }

    public static void backTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_right);
    }

}
