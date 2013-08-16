package me.sheimi.sgit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;

/**
 * Created by sheimi on 8/6/13.
 */
public class RepoListAdapter extends CursorAdapter implements RepoDbManager
        .RepoDbObserver {

    private static final int QUERY_TYPE_SEARCH = 0;
    private static final int QUERY_TYPE_QUERY = 1;

    private RepoDbManager mDb;

    private int mQueryType = QUERY_TYPE_QUERY;
    private String mSearchQueryString;

    public RepoListAdapter(Context context) {
        super(context, null, true);
        mDb = RepoDbManager.getInstance(context);
        mDb.registerDbObserver(RepoContract.RepoEntry.TABLE_NAME, this);
    }

    public void searchDiary(String query) {
        mQueryType = QUERY_TYPE_SEARCH;
        mSearchQueryString = query;
        requery();
    }

    public void queryAllDiary() {
        mQueryType = QUERY_TYPE_QUERY;
        requery();
    }

    private void requery() {
        Cursor cursor = null;
        switch (mQueryType) {
            case QUERY_TYPE_SEARCH:
                cursor = mDb.searchRepo(mSearchQueryString);
                break;
            case QUERY_TYPE_QUERY:
                cursor = mDb.queryAllRepo();
                break;
        }
        changeCursor(cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.repo_listitem, viewGroup, false);
        RepoListItemHolder holder = new RepoListItemHolder();
        view.setTag(holder);
        holder.repoTitle = (TextView) view.findViewById(R.id.repoTitle);
        String title = getRepoTitle(cursor);
        holder.repoTitle.setText(title);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title = getRepoTitle(cursor);
        RepoListItemHolder holder = (RepoListItemHolder) view.getTag();
        holder.repoTitle.setText(title);
    }

    @Override
    public void nofityChanged() {
        Cursor cursor = getCursor();
        if (cursor == null)
            return;
        requery();
    }

    public static String getRepoTitle(Cursor cursor) {
        return cursor.getString(1);
    }

    public static int getRepoID(Cursor cursor) {
        return cursor.getInt(0);
    }

    private static class RepoListItemHolder {
        public TextView repoTitle;
    }

}
