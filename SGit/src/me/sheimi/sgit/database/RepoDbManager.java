package me.sheimi.sgit.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.sheimi.android.utils.BasicFunctions;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by sheimi on 8/7/13.
 */
public class RepoDbManager {

    private static RepoDbManager mInstance;

    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    private RepoDbHelper mDbHelper;

    private static Map<String, Set<RepoDbObserver>> mObservers = new HashMap<String, Set<RepoDbObserver>>();

    private RepoDbManager(Context context) {
        mDbHelper = new RepoDbHelper(context);
        mWritableDatabase = mDbHelper.getWritableDatabase();
        mReadableDatabase = mDbHelper.getReadableDatabase();
    }

    private static RepoDbManager getInstance() {
        if (mInstance == null) {
            mInstance = new RepoDbManager(BasicFunctions.getActiveActivity());
        }
        return mInstance;
    }

    public static void registerDbObserver(String table, RepoDbObserver observer) {
        Set<RepoDbObserver> set = mObservers.get(table);
        if (set == null) {
            set = new HashSet<RepoDbObserver>();
            mObservers.put(table, set);
        }
        set.add(observer);
    }

    public static void unregisterDbObserver(String table,
            RepoDbObserver observer) {
        Set<RepoDbObserver> set = mObservers.get(table);
        if (set == null)
            return;
        set.remove(observer);
    }

    public static void notifyObservers(String table) {
        Set<RepoDbObserver> set = mObservers.get(table);
        if (set == null)
            return;
        for (RepoDbObserver observer : set) {
            observer.nofityChanged();
        }
    }

    public static interface RepoDbObserver {
        public void nofityChanged();
    }

    public static Cursor searchRepo(String query) {
        return getInstance()._searchRepo(query);
    }

    private Cursor _searchRepo(String query) {
        String selection = RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH
                + " LIKE ? OR " + RepoContract.RepoEntry.COLUMN_NAME_REMOTE_URL
                + " LIKE ? OR "
                + RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMITTER_UNAME
                + " LIKE ? OR "
                + RepoContract.RepoEntry.COLUMN_NAME_LATEST_COMMIT_MSG
                + " LIKE ?";
        query = "%" + query + "%";
        String[] selectionArgs = { query, query, query, query };
        Cursor cursor = mReadableDatabase.query(true,
                RepoContract.RepoEntry.TABLE_NAME,
                RepoContract.RepoEntry.ALL_COLUMNS, selection, selectionArgs,
                null, null, null, null);
        return cursor;
    }

    public static Cursor queryAllRepo() {
        return getInstance()._queryAllRepo();
    }

    private Cursor _queryAllRepo() {
        Cursor cursor = mReadableDatabase.query(true,
                RepoContract.RepoEntry.TABLE_NAME,
                RepoContract.RepoEntry.ALL_COLUMNS, null, null, null, null,
                null, null);
        return cursor;
    }

    public static Cursor getRepoById(long id) {
        return getInstance()._getRepoById(id);
    }

    private Cursor _getRepoById(long id) {
        Cursor cursor = mReadableDatabase.query(true,
                RepoContract.RepoEntry.TABLE_NAME,
                RepoContract.RepoEntry.ALL_COLUMNS, RepoContract.RepoEntry._ID
                        + "= ?", new String[] { String.valueOf(id) }, null,
                null, null, null);
        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public static long insertRepo(ContentValues values) {
        return getInstance()._insertRepo(values);
    }

    private long _insertRepo(ContentValues values) {
        long id = mWritableDatabase.insert(RepoContract.RepoEntry.TABLE_NAME,
                null, values);
        notifyObservers(RepoContract.RepoEntry.TABLE_NAME);
        return id;
    }

    public static void updateRepo(long id, ContentValues values) {
        getInstance()._updateRepo(id, values);
    }

    private void _updateRepo(long id, ContentValues values) {
        String selection = RepoContract.RepoEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        mWritableDatabase.update(RepoContract.RepoEntry.TABLE_NAME, values,
                selection, selectionArgs);
        notifyObservers(RepoContract.RepoEntry.TABLE_NAME);
    }

    public static void deleteRepo(long id) {
        getInstance()._deleteRepo(id);
    }

    private void _deleteRepo(long id) {
        String selection = RepoContract.RepoEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        mWritableDatabase.delete(RepoContract.RepoEntry.TABLE_NAME, selection,
                selectionArgs);
        notifyObservers(RepoContract.RepoEntry.TABLE_NAME);
    }

}
