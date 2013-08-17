package me.sheimi.sgit.database;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by sheimi on 8/6/13.
 */
public final class RepoContract {

    private static final String TEXT_TYPE = " TEXT ";
    private static final String INT_TYPE = " INTEGER ";
    private static final String PRIMARY_KEY_TYPE = INT_TYPE + "PRIMARY KEY " +
            "AUTOINCREMENT ";
    private static final String COMMA_SEP = ",";

    public RepoContract() {}

    public static abstract class RepoEntry implements BaseColumns {
        public static final String TABLE_NAME = "repo";
        public static final String COLUMN_NAME_LOCAL_PATH = "local_path";
        public static final String COLUMN_NAME_REMOTE_URL = "remote_url";
        public static final String [] ALL_COLUMNS = {
            _ID, COLUMN_NAME_LOCAL_PATH, COLUMN_NAME_REMOTE_URL,
        };
    }

    public static int getRepoID(Cursor cursor) {
        return cursor.getInt(0);
    }

    public static String getLocalPath(Cursor cursor) {
        return cursor.getString(1);
    }

    public static String getRemoteURL(Cursor cursor) {
        return cursor.getString(2);
    }

    public static final String REPO_ENTRY_CREATE =
            "CREATE TABLE " + RepoEntry.TABLE_NAME + " ("
                    + RepoEntry._ID + PRIMARY_KEY_TYPE + COMMA_SEP
                    + RepoEntry.COLUMN_NAME_LOCAL_PATH + TEXT_TYPE + COMMA_SEP
                    + RepoEntry.COLUMN_NAME_REMOTE_URL + TEXT_TYPE
                    + " )";


    public static final String REPO_ENTRY_DROP =
            "DROP TABLE IF EXISTS " + RepoEntry.TABLE_NAME;


}
