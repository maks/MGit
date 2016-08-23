package me.sheimi.sgit.adapters;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.RepoListActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.database.models.Repo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sheimi on 8/6/13.
 */
public class RepoListAdapter extends ArrayAdapter<Repo> implements
        RepoDbManager.RepoDbObserver, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final int QUERY_TYPE_SEARCH = 0;
    private static final int QUERY_TYPE_QUERY = 1;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new SimpleDateFormat(
            "MM/dd/yyyy", Locale.getDefault());

    private int mQueryType = QUERY_TYPE_QUERY;
    private String mSearchQueryString;
    private RepoListActivity mActivity;

    public RepoListAdapter(Context context) {
        super(context, 0);
        RepoDbManager.registerDbObserver(RepoContract.RepoEntry.TABLE_NAME,
                this);
        mActivity = (RepoListActivity) context;
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
                cursor = RepoDbManager.searchRepo(mSearchQueryString);
                break;
            case QUERY_TYPE_QUERY:
                cursor = RepoDbManager.queryAllRepo();
                break;
        }
        List<Repo> repo = Repo.getRepoList(mActivity, cursor);
        Collections.sort(repo);
        cursor.close();
        clear();
        addAll(repo);
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
        holder.cancelBtn = (ImageView) view.findViewById(R.id.cancelBtn);
        view.setTag(holder);
        return view;
    }

    public void bindView(View view, int position) {
        RepoListItemHolder holder = (RepoListItemHolder) view.getTag();
        final Repo repo = getItem(position);

        holder.repoTitle.setText(repo.getDiaplayName());
        holder.repoRemote.setText(repo.getRemoteURL());

        if (!repo.getRepoStatus().equals(RepoContract.REPO_STATUS_NULL)) {
            holder.commitMsgContainer.setVisibility(View.GONE);
            holder.progressContainer.setVisibility(View.VISIBLE);
            holder.progressMsg.setText(repo.getRepoStatus());
            holder.cancelBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    repo.deleteRepo();
                    repo.cancelTask();
                }
            });
        } else if (repo.getLastCommitter() != null) {
            holder.commitMsgContainer.setVisibility(View.VISIBLE);
            holder.progressContainer.setVisibility(View.GONE);

            String date = "";
            if (repo.getLastCommitDate() != null) {
                date = COMMITTIME_FORMATTER.format(repo.getLastCommitDate());
            }
            holder.commitTime.setText(date);
            holder.commitMsg.setText(repo.getLastCommitMsg());
            holder.commitAuthor.setText(repo.getLastCommitter());
            holder.authorIcon.setVisibility(View.VISIBLE);
            holder.authorIcon.setImageResource(R.drawable.ic_default_author);
            String authorIconURL = BasicFunctions.buildGravatarURL(repo
                    .getLastCommitterEmail());
            BasicFunctions.getImageLoader().displayImage(authorIconURL,
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
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        Repo repo = getItem(position);
        Intent intent = new Intent(mActivity, RepoDetailActivity.class);
        intent.putExtra(Repo.TAG, repo);
        mActivity.startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        final Repo repo = getItem(position);
        if (!repo.getRepoStatus().equals(RepoContract.REPO_STATUS_NULL))
            return false;
        Context context = getContext();
        if (context instanceof SheimiFragmentActivity) {
            showRepoOptionsDialog((SheimiFragmentActivity) context, repo);
        }
        return true;
    }

    private void showRepoOptionsDialog(final SheimiFragmentActivity context, final Repo repo) {
        context.showOptionsDialog(
            R.string.dialog_choose_option,
            R.array.dialog_choose_repo_action_items,
            new SheimiFragmentActivity.onOptionDialogClicked[] {
                new SheimiFragmentActivity.onOptionDialogClicked() {
                    @Override
                    public void onClicked() {
                        showRenameRepoDialog(context, repo);
                    }
                },
                new SheimiFragmentActivity.onOptionDialogClicked() {
                    @Override
                    public void onClicked() {
                        showRemoveRepoDialog(context, repo);
                    }
                }
            }
        );
    }

    private void showRemoveRepoDialog(SheimiFragmentActivity context, final Repo repo) {
        context.showMessageDialog(
            R.string.dialog_delete_repo_title,
            R.string.dialog_delete_repo_msg,
            R.string.label_delete,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    repo.deleteRepo();
                    repo.cancelTask();
                }
            }
        );
    }

    private void showRenameRepoDialog(final SheimiFragmentActivity context, final Repo repo) {
        context.showEditTextDialog(
            R.string.dialog_rename_repo_title,
            R.string.dialog_rename_repo_hint,
            R.string.label_rename,
            new SheimiFragmentActivity.OnEditTextDialogClicked() {
                @Override
                public void onClicked(String newRepoName) {
                    if (!repo.renameRepo(newRepoName)){
                        context.showToastMessage(R.string.error_rename_repo_fail);
                    }
                }
            }
        );
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
        public TextView progressMsg;
        public ImageView cancelBtn;
    }

}
