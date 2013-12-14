package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class ChooseCommitDialog extends DialogFragment {

    private Repo mRepo;
    private RepoDetailActivity mActivity;
    private ViewUtils mViewUtils;
    private ListView mBranchTagList;
    private BranchTagListAdapter mAdapter;

    public ChooseCommitDialog() {
    }

    public ChooseCommitDialog(Repo repo) {
        mRepo = repo;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Repo.TAG, mRepo);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
            mRepo.setContext(getActivity());
        }

        mActivity = (RepoDetailActivity) getActivity();
        mViewUtils = ViewUtils.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        mBranchTagList = new ListView(mActivity);
        mAdapter = new BranchTagListAdapter(mActivity);
        mBranchTagList.setAdapter(mAdapter);
        builder.setView(mBranchTagList);

        String[] branches = mRepo.getBranches();
        String[] tags = mRepo.getTags();
        mViewUtils.adapterAddAll(mAdapter, branches);
        mViewUtils.adapterAddAll(mAdapter, tags);

        builder.setTitle(R.string.dialog_choose_branch_title);
        mBranchTagList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        String commitName = mAdapter.getItem(position);
                        mActivity.resetCommits(commitName);
                        getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private class BranchTagListAdapter extends ArrayAdapter<String> {

        public BranchTagListAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ListItemHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(
                        R.layout.listitem_dialog_choose_commit, parent, false);
                holder = new ListItemHolder();
                holder.commitTitle = (TextView) convertView
                        .findViewById(R.id.commitTitle);
                holder.commitIcon = (ImageView) convertView
                        .findViewById(R.id.commitIcon);
                convertView.setTag(holder);
            } else {
                holder = (ListItemHolder) convertView.getTag();
            }
            String commitName = getItem(position);
            String displayName = Repo.getCommitDisplayName(commitName);
            int commitType = Repo.getCommitType(commitName);
            switch (commitType) {
                case Repo.COMMIT_TYPE_HEAD:
                    holder.commitIcon.setImageResource(R.drawable.ic_branch_d);
                    break;
                case Repo.COMMIT_TYPE_TAG:
                    holder.commitIcon.setImageResource(R.drawable.ic_tag_d);
                    break;
            }
            holder.commitTitle.setText(displayName);
            return convertView;
        }

    }

    private static class ListItemHolder {
        public TextView commitTitle;
        public ImageView commitIcon;
    }

}
