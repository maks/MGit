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

import org.eclipse.jgit.api.Git;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class ChooseCommitDialog extends DialogFragment {

    private Git mGit;
    private RepoDetailActivity mActivity;
    private RepoUtils mRepoUtils;
    private ListView mBranchTagList;

    public ChooseCommitDialog(Git git) {
        mGit = git;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (RepoDetailActivity) getActivity();
        mRepoUtils = (RepoUtils) RepoUtils.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        ListView mBranchTagList = new ListView(mActivity);
        final BranchTagListAdapter mAdapter = new BranchTagListAdapter
                (mActivity);
        mBranchTagList.setAdapter(mAdapter);
        builder.setView(mBranchTagList);

        String[] branches = mRepoUtils.getBranches(mGit);
        String[] tags = mRepoUtils.getTags(mGit);

        mAdapter.addAll(branches);
        mAdapter.addAll(tags);

        builder.setTitle(R.string.dialog_choose_branch_title);
        mBranchTagList.setOnItemClickListener(new AdapterView
                .OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
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
                convertView = inflater.inflate(R.layout
                        .listitem_dialog_choose_commit, parent, false);
                holder = new ListItemHolder();
                holder.commitTitle = (TextView) convertView.findViewById(R.id
                        .commitTitle);
                holder.commitIcon = (ImageView) convertView.findViewById(R.id
                        .commitIcon);
                convertView.setTag(holder);
            } else {
                holder = (ListItemHolder) convertView.getTag();
            }
            String commitName = getItem(position);
            String displayName = mRepoUtils.getCommitDisplayName(commitName);
            int commitType = mRepoUtils.getCommitType(commitName);
            switch (commitType) {
                case RepoUtils.COMMIT_TYPE_HEAD:
                    holder.commitIcon.setImageResource(R.drawable.ic_branch_d);
                    break;
                case RepoUtils.COMMIT_TYPE_TAG:
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
