package me.sheimi.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

/**
 * Created by lee on 2015-02-01.
 */
public class Profile {

    private static final String GIT_USER_NAME = "user.name";
    private static final String GIT_USER_EMAIL = "user.email";
    private static SharedPreferences sSharedPreference;

    private static boolean sHasLastCloneFail = false;
    private static Repo sLastFailRepo;

    private static SharedPreferences getProfileSharedPreference() {
        if (sSharedPreference == null) {
            sSharedPreference = BasicFunctions.getActiveActivity().getSharedPreferences(
                                    BasicFunctions.getActiveActivity().getString(R.string.preference_file_key),
                                    Context.MODE_PRIVATE);
        }
        return sSharedPreference;
    }

    public static String getUsername() {
        return getProfileSharedPreference().getString(GIT_USER_NAME, "");
    }

    public static String getEmail() {
        return getProfileSharedPreference().getString(GIT_USER_EMAIL, "");
    }

    public static void setProfileInformation(String username, String email) {
        SharedPreferences.Editor editor = getProfileSharedPreference().edit();
        editor.putString(Profile.GIT_USER_NAME, username);
        editor.putString(Profile.GIT_USER_EMAIL, email);
        editor.commit();
    }

    public static boolean hasLastCloneFailed() {
        return sHasLastCloneFail;
    }

    public static Repo getLastCloneTryRepo() {
        return sLastFailRepo;
    }

    public static void setLastCloneFailed(Repo repo) {
        sHasLastCloneFail = true;
        sLastFailRepo = repo;
    }

    public static void setLastCloneSuccess() {
        sHasLastCloneFail = false;
    }
}












