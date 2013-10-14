package me.sheimi.sgit.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.jgit.lib.ProgressMonitor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.ImageCache;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/6/13.
 */
public class RepoListAdapter extends ArrayAdapter<Repo> implements RepoDbManager
        .RepoDbObserver, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int QUERY_TYPE_SEARCH = 0;
    private static final int QUERY_TYPE_QUERY = 1;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    private RepoDbManager mDb;

    private int mQueryType = QUERY_TYPE_QUERY;
    private String mSearchQueryString;
    private RepoListActivity mActivity;
    private ViewUtils mViewUtils;
    private RepoUtils mRepoUtils;

    private SparseArray<Integer> mCloningProgress = new SparseArray<Integer>();

    public RepoListAdapter(Context context) {
        super(context, 0);
        mDb = RepoDbManager.getInstance(context);
        mDb.registerDbObserver(RepoContract.RepoEntry.TABLE_NAME, this);
        mActivity = (RepoListActivity) context;
        mViewUtils = ViewUtils.getInstance(context);
        mRepoUtils = RepoUtils.getInstance(context);
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
        List<Repo> repo = Repo.getRepoList(mActivity, cursor);
        Collections.sort(repo);
        cursor.close();
        clear();
        mViewUtils.adapterAddAll(this, repo);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(getContext(), parent);
        }
        bindView(convertView, position);
        return convertView;
    }

    public View newView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.repo_listitem, parent, false);
        RepoListItemHolder holder = new RepoListItemHolder();
        holder.repoTitle = (TextView) view.findViewById(R.id.repoTitle);
        holder.repoRemote = (TextView) view.findViewById(R.id.repoRemote);
        holder.commitAuthor = (TextView) view.findViewById(R.id.commitAuthor);
        holder.commitMsg = (TextView) view.findViewById(R.id.commitMsg);
        holder.commitTime = (TextView) view.findViewById(R.id.commitTime);
        holder.authorIcon = (ImageView) view.findViewById(R.id.authorIcon);
        holder.progressContainer = view.findViewById(R.id.progressContainer);
        holder.commitMsgContainer = view.findViewById(R.id.commitMsgContainer);
        holder.progressMsg = (TextView) view.findViewById(R.id.progressMsg);
        holder.cloningProgressBar = (ProgressBar) view.findViewById(R.id.cloningProgressBar);
        view.setTag(holder);
        return view;
    }

    public void bindView(View view, int position) {
        RepoListItemHolder holder = (RepoListItemHolder) view.getTag();
        Repo repo = getItem(position);

        holder.repoTitle.setText(repo.getLocalPath());
        holder.repoRemote.setText(repo.getRemoteURL());

        if (!repo.getRepoStatus().equals(RepoContract.REPO_STATUS_NULL)) {
            holder.commitMsgContainer.setVisibility(View.GONE);
            holder.progressContainer.setVisibility(View.VISIBLE);
            int progress = mCloningProgress.get(repo.getID(), -1);
            if (progress != -1) {
                holder.progressMsg.setText(String.format("%s  (%d%%)", repo.getRepoStatus(),
                        progress));
            } else {
                holder.progressMsg.setText(String.format("%s", repo.getRepoStatus()));
            }
        } else if (repo.getLastCommitter() != null) {
            holder.commitMsgContainer.setVisibility(View.VISIBLE);
            holder.progressContainer.setVisibility(View.GONE);

            holder.commitTime.setText(COMMITTIME_FORMATTER.format(repo.getLastCommitDate()));
            holder.commitMsg.setText(repo.getLastCommitMsg());
            holder.commitAuthor.setText(repo.getLastCommitter());
            holder.authorIcon.setVisibility(View.VISIBLE);
            holder.authorIcon.setImageResource(R.drawable.ic_default_author);
            String authorIconURL = CommonUtils.buildGravatarURL(repo.getLastCommitterEmail());
            ImageCache.getInstance(mActivity).getImageLoader().displayImage(authorIconURL,
                    holder.authorIcon);
        }
    }

    @Override
    public void nofityChanged() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requery();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Repo repo = getItem(position);
        if (!repo.getRepoStatus().equals(RepoContract.REPO_STATUS_NULL))
            return;
        Intent intent = new Intent(mActivity, RepoDetailActivity.class);
        intent.putExtra(Repo.TAG, repo);
        ActivityUtils.startActivity(mActivity, intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        final Repo repo = getItem(position);
        if (!repo.getRepoStatus().equals(RepoContract.REPO_STATUS_NULL))
            return false;
        mViewUtils.showMessageDialog(R.string.dialog_delete_repo_title,
                R.string.dialog_delete_repo_msg, R.string.label_delete, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                repo.deleteRepo();
            }
        });
        return true;
    }

    public ProgressMonitor getCloneMonitor(long id) {
        return new CloningMonitor(id);
    }

    private class RepoListItemHolder {
        public TextView repoTitle;
        public TextView repoRemote;
        public TextView commitAuthor;
        public TextView commitMsg;
        public TextView commitTime;
        public ImageView authorIcon;
        public View progressContainer;
        public View commitMsgContainer;
        public ProgressBar cloningProgressBar;
        public TextView progressMsg;
    }

    public class CloningMonitor implements ProgressMonitor {

        private long mID;
        private int mTotalWork;
        private int mWorkDone;

        public CloningMonitor(long id) {
            mID = id;
        }

        @Override
        public void start(int totalTasks) {
            mCloningProgress.put((int) mID, 0);
        }

        @Override
        public void beginTask(String title, int totalWork) {
            mTotalWork = totalWork;
            mWorkDone = 0;
            mCloningProgress.put((int) mID, 0);
            if (title != null) {
                ContentValues values = new ContentValues();
                values.put(RepoContract.RepoEntry.COLUMN_NAME_REPO_STATUS,
                        title + " ...");
                mDb.updateRepo(mID, values);
            }
        }

        @Override
        public void update(int i) {
            mWorkDone += i;
            if (mTotalWork != 0) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = computeProgress();
                        mCloningProgress.put((int) mID, progress);
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void endTask() {

        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        private int computeProgress() {
            return 100 * mWorkDone / mTotalWork;
        }

    }

}
