package me.sheimi.sgit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sheimi on 8/7/13.
 */
public class RepoDbManager {

    private static RepoDbManager mInstance;

    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    private RepoDbHelper mDbHelper;

    private Map<String, Set<RepoDbObserver>> mObservers;

    private RepoDbManager(Context context) {
        mDbHelper = new RepoDbHelper(context);
        mWritableDatabase = mDbHelper.getWritableDatabase();
        mReadableDatabase = mDbHelper.getReadableDatabase();
        mObservers = new HashMap<String, Set<RepoDbObserver>>();
    }

    public static RepoDbManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RepoDbManager(context);
        }
        return mInstance;
    }

    public void registerDbObserver(String table, RepoDbObserver observer) {
        Set<RepoDbObserver> set = mObservers.get(table);
        if (set == null) {
            set = new HashSet<RepoDbObserver>();
            mObservers.put(table, set);
        }
        set.add(observer);
    }

    public void unregisterDbObserver(String table, RepoDbObserver observer) {
        Set<RepoDbObserver> set = mObservers.get(table);
        if (set == null)
            return;
        set.remove(observer);
    }

    public void notifyObservers(String table) {
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

    public Cursor searchRepo(String query) {
        Cursor cursor = mReadableDatabase.query(true, RepoContract.RepoEntry
                .TABLE_NAME, RepoContract.RepoEntry.ALL_COLUMNS,
                RepoContract.RepoEntry.COLUMN_NAME_LOCAL_PATH + " LIKE ?",
                new String[]{"%" + query + "%"}, null, null, null, null);
        return cursor;
    }

    public Cursor queryAllRepo() {
        Cursor cursor = mReadableDatabase.query(true, RepoContract.RepoEntry
                .TABLE_NAME, RepoContract.RepoEntry.ALL_COLUMNS,
                null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getRepoById(int id) {
        Cursor cursor = mReadableDatabase.query(true, RepoContract.RepoEntry
                .TABLE_NAME, RepoContract.RepoEntry.ALL_COLUMNS,
                RepoContract.RepoEntry._ID + "= ?", new String[]{String
                .valueOf(id)}, null, null, null, null);
        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insertRepo(ContentValues values) {
        long id = mWritableDatabase.insert(RepoContract.RepoEntry.TABLE_NAME,
                null, values);
        notifyObservers(RepoContract.RepoEntry.TABLE_NAME);
        return id;
    }

    public void updateRepo(long id, ContentValues values) {
        String selection = RepoContract.RepoEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        mWritableDatabase.update(RepoContract.RepoEntry.TABLE_NAME, values,
                selection, selectionArgs);
        notifyObservers(RepoContract.RepoEntry.TABLE_NAME);
    }

}
