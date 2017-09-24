package me.sheimi.sgit.database;

import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by sheimi on 8/6/13.
 */
public final class RepoContract {

    private static final String TEXT_TYPE = " TEXT ";
    private static final String INT_TYPE = " INTEGER ";
    private static final String PRIMARY_KEY_TYPE = INT_TYPE + "PRIMARY KEY "
            + "AUTOINCREMENT ";
    private static final String COMMA_SEP = ",";
    public static final String REPO_STATUS_NULL = "";

    public RepoContract() {
    }

    public static abstract class RepoEntry implements BaseColumns {
        public static final String TABLE_NAME = "repo";
        public static final String COLUMN_NAME_LOCAL_PATH = "local_path";
        public static final String COLUMN_NAME_REMOTE_URL = "remote_url";
        public static final String COLUMN_NAME_REPO_STATUS = "repo_status";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
        // latest commit's committer name
        public static final String COLUMN_NAME_LATEST_COMMITTER_UNAME = "latest_committer_uname";
        public static final String COLUMN_NAME_LATEST_COMMITTER_EMAIL = "latest_committer_email";
        public static final String COLUMN_NAME_LATEST_COMMIT_DATE = "latest_commit_date";
        public static final String COLUMN_NAME_LATEST_COMMIT_MSG = "latest_commit_msg";
        public static final String[] ALL_COLUMNS = { _ID,
                COLUMN_NAME_LOCAL_PATH, COLUMN_NAME_REMOTE_URL,
                COLUMN_NAME_REPO_STATUS, COLUMN_NAME_LATEST_COMMITTER_UNAME,
                COLUMN_NAME_LATEST_COMMITTER_EMAIL,
                COLUMN_NAME_LATEST_COMMIT_DATE, COLUMN_NAME_LATEST_COMMIT_MSG,
                COLUMN_NAME_USERNAME, COLUMN_NAME_PASSWORD };
    }

    public static final String REPO_ENTRY_CREATE = "CREATE TABLE "
            + RepoEntry.TABLE_NAME + " (" + RepoEntry._ID + PRIMARY_KEY_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_LOCAL_PATH + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_REMOTE_URL + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_USERNAME + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_PASSWORD + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_REPO_STATUS + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_LATEST_COMMITTER_UNAME
            + TEXT_TYPE + COMMA_SEP
            + RepoEntry.COLUMN_NAME_LATEST_COMMITTER_EMAIL + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_LATEST_COMMIT_DATE + TEXT_TYPE
            + COMMA_SEP + RepoEntry.COLUMN_NAME_LATEST_COMMIT_MSG + TEXT_TYPE
            + " )";

    public static final String REPO_ENTRY_DROP = "DROP TABLE IF EXISTS "
            + RepoEntry.TABLE_NAME;

    public static int getRepoID(Cursor cursor) {
        return cursor.getInt(0);
    }

    public static String getLocalPath(Cursor cursor) {
        return cursor.getString(1);
    }

    public static String getRemoteURL(Cursor cursor) {
        return cursor.getString(2);
    }

    public static String getRepoStatus(Cursor cursor) {
        return cursor.getString(3);
    }

    public static String getLatestCommitterName(Cursor cursor) {
        return cursor.getString(4);
    }

    public static String getLatestCommitterEmail(Cursor cursor) {
        return cursor.getString(5);
    }

    public static Date getLatestCommitDate(Cursor cursor) {
        String longStr = cursor.getString(6);
        if (longStr == null || longStr.isEmpty()) {
            return null;
        }
        long time = Long.parseLong(longStr);
        return new Date(time);
    }

    public static String getLatestCommitMsg(Cursor cursor) {
        return cursor.getString(7);
    }

    public static String getUsername(Cursor cursor) {
        return cursor.getString(8);
    }

    public static String getPassword(Cursor cursor) {
        return cursor.getString(9);
    }

}
