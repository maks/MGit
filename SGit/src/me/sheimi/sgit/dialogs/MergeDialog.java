package me.sheimi.sgit.dialogs;

import java.util.List;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.database.models.Repo;

import org.eclipse.jgit.lib.Ref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by sheimi on 8/16/13.
 */
public class MergeDialog extends SheimiDialogFragment {

    private Repo mRepo;
    private RepoDetailActivity mActivity;
    private ListView mBranchTagList;
    private Spinner mSpinner;
    private BranchTagListAdapter mAdapter;
    private CheckBox mCheckbox;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Repo.TAG, mRepo);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(Repo.TAG)) {
            mRepo = (Repo) args.getSerializable(Repo.TAG);
        }
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }

        mActivity = (RepoDetailActivity) getActivity();
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_merge, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        mBranchTagList = (ListView) layout.findViewById(R.id.branchList);
        mSpinner = (Spinner) layout.findViewById(R.id.ffSpinner);
        mCheckbox = (CheckBox) layout.findViewById(R.id.autoCommit);
        mAdapter = new BranchTagListAdapter(mActivity);
        mBranchTagList.setAdapter(mAdapter);
        builder.setView(layout);

        List<Ref> branches = mRepo.getLocalBranches();
        String currentBranchDisplayName = mRepo.getCurrentDisplayName();
        for (Ref branch : branches) {
            if (Repo.getCommitDisplayName(branch.getName()).equals(
                    currentBranchDisplayName))
                continue;
            mAdapter.add(branch);
        }

        builder.setTitle(R.string.dialog_merge_title);
        mBranchTagList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        Ref commit = mAdapter.getItem(position);
                        String mFFString = mSpinner.getSelectedItem()
                                .toString();
                        mActivity.mergeBranch(commit, mFFString,
                                mCheckbox.isChecked());
                        getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private class BranchTagListAdapter extends ArrayAdapter<Ref> {

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
            String commitName = getItem(position).getName();
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
