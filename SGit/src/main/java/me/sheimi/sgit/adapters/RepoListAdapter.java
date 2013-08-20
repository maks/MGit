package me.sheimi.sgit.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.ImageCache;

/**
 * Created by sheimi on 8/6/13.
 */
public class RepoListAdapter extends CursorAdapter implements RepoDbManager
        .RepoDbObserver {

    private static final int QUERY_TYPE_SEARCH = 0;
    private static final int QUERY_TYPE_QUERY = 1;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    private RepoDbManager mDb;

    private int mQueryType = QUERY_TYPE_QUERY;
    private String mSearchQueryString;
    private Activity mActivity;

    public RepoListAdapter(Context context) {
        super(context, null, true);
        mDb = RepoDbManager.getInstance(context);
        mDb.registerDbObserver(RepoContract.RepoEntry.TABLE_NAME, this);
        mActivity = (Activity) context;
    }

    public void searchRepo(String query) {
        mQueryType = QUERY_TYPE_SEARCH;
        mSearchQueryString = query;
        requery();
    }

    public void queryAllRepo() {
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
        holder.repoTitle = (TextView) view.findViewById(R.id.repoTitle);
        holder.repoRemote = (TextView) view.findViewById(R.id.repoRemote);
        holder.commitAuthor = (TextView) view.findViewById(R.id.commitAuthor);
        holder.commitMsg = (TextView) view.findViewById(R.id.commitMsg);
        holder.commitTime = (TextView) view.findViewById(R.id.commitTime);
        holder.authorIcon = (ImageView) view.findViewById(R.id.authorIcon);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String repoTitle = RepoContract.getLocalPath(cursor);
        boolean isCloning = RepoContract.isCloning(cursor);
        String repoRemote = RepoContract.getRemoteURL(cursor);
        Date commitTime = RepoContract.getLatestCommitDate(cursor);
        String commitMsg = RepoContract.getLatestCommitMsg(cursor);
        String commitAuthor = RepoContract.getLatestCommitterName(cursor);
        String committerEmail = RepoContract.getLatestCommitterEmail(cursor);
        if (isCloning) {
            repoTitle += " cloning ...";
        }
        RepoListItemHolder holder = (RepoListItemHolder) view.getTag();
        if (repoTitle != null) {
            holder.repoTitle.setText(repoTitle);
        }
        if (repoRemote != null) {
            holder.repoRemote.setText(repoRemote);
        }
        if (commitTime != null) {
            holder.commitTime.setText(COMMITTIME_FORMATTER.format(commitTime));
        }
        if (commitMsg != null) {
            holder.commitMsg.setText(commitMsg);
        }
        if (commitAuthor != null) {
            holder.commitAuthor.setText(commitAuthor);
        }
        if (committerEmail != null) {
            holder.authorIcon.setVisibility(View.VISIBLE);
            holder.authorIcon.setImageResource(R.drawable.ic_default_author);
            String authorIconURL = CommonUtils.buildGravatarURL(committerEmail);
            ImageCache.getInstance(mActivity).getImageLoader().displayImage(authorIconURL,
                    holder.authorIcon);
        } else {
            holder.authorIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void nofityChanged() {
        Cursor cursor = getCursor();
        if (cursor == null)
            return;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requery();
            }
        });
    }

    private class RepoListItemHolder {
        public TextView repoTitle;
        public TextView repoRemote;
        public TextView commitAuthor;
        public TextView commitMsg;
        public TextView commitTime;
        public ImageView authorIcon;

    }

}
