package me.sheimi.sgit.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.CommitDiffActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.CommitsListAdapter;
import me.sheimi.sgit.dialogs.ChooseCommitDialog;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/5/13.
 */
public class CommitsFragment extends BaseFragment implements ActionMode.Callback {

    private final static String LOCAL_REPO = "local repo";
    private final static String IS_ACTION_MODE = "is action mode";
    private final static String CHOSEN_ITEM = "chosen item";

    private String mLocalRepo;

    private RepoDetailActivity mActivity;
    private Button mCommitNameButton;
    private ImageView mCommitType;
    private ListView mCommitsList;
    private CommitsListAdapter mCommitsListAdapter;

    private RepoUtils mRepoUtils;
    private ViewUtils mViewUtils;
    private Git mGit;
    private ActionMode mActionMode;
    private Set<Integer> mChosenItem = new HashSet<Integer>();

    public static CommitsFragment newInstance(String mLocalRepo) {
        CommitsFragment fragment = new CommitsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LOCAL_REPO, mLocalRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_commits, container, false);
        mRepoUtils = RepoUtils.getInstance(mActivity);
        mViewUtils = ViewUtils.getInstance(mActivity);
        mActivity = (RepoDetailActivity) getActivity();
        mActivity.setCommitsFragment(this);

        Bundle bundle = getArguments();
        String localRepoStr = bundle.getString(LOCAL_REPO);
        if (localRepoStr != null) {
            mLocalRepo = localRepoStr;
        }
        if (savedInstanceState != null) {
            String saveRepoStr = savedInstanceState.getString(LOCAL_REPO);
            if (saveRepoStr != null) {
                mLocalRepo = saveRepoStr;
            }
        }
        if (mLocalRepo == null) {
            // this will not execute, if the app runs right
            return v;
        }
        mGit = mRepoUtils.getGit(mLocalRepo);

        mCommitsList = (ListView) v.findViewById(R.id.commitsList);
        mCommitNameButton = (Button) v.findViewById(R.id.commitName);
        mCommitType = (ImageView) v.findViewById(R.id.commitType);
        mCommitsListAdapter = new CommitsListAdapter(mActivity, mChosenItem);
        mCommitsListAdapter.resetCommit(mGit);
        mCommitsList.setAdapter(mCommitsListAdapter);


        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCommitDialog cbd = new ChooseCommitDialog(mGit);
                cbd.show(getFragmentManager(), "choose-branch-dialog");
            }
        });
        mCommitsList.setOnItemClickListener(new AdapterView
                .OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                if (mActionMode == null) {
                    if (position == mCommitsListAdapter.getCount() - 1) {
                        mViewUtils.showToastMessage(R.string.alert_no_older_commits);

                    }

                    RevCommit commit = mCommitsListAdapter.getItem(position);
                    String fullCommitName = commit.getName();
                    mActivity.resetCommits(fullCommitName);
                    return;
                }
                chooseItem(position);
            }
        });
        mCommitsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long l) {
                if (mActionMode != null) {
                    return false;
                }
                enterDiffActionMode();
                chooseItem(position);
                return true;
            }
        });

        String branchName = mRepoUtils.getBranchName(mGit);
        reset(branchName);
        return v;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        boolean isActionMode = savedInstanceState.getBoolean(IS_ACTION_MODE);
        if (isActionMode) {
            List<Integer> itemsInt = savedInstanceState.getIntegerArrayList(CHOSEN_ITEM);
            mActionMode = getActivity().startActionMode(this);
            mChosenItem.addAll(itemsInt);
            mCommitsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCAL_REPO, mLocalRepo);
        outState.putBoolean(IS_ACTION_MODE, mActionMode != null);
        ArrayList<Integer> itemsList = new ArrayList<Integer>(mChosenItem);
        outState.putIntegerArrayList(CHOSEN_ITEM, itemsList);
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }

    public void reset(String commitName) {
        int commitType = mRepoUtils.getCommitType(commitName);
        switch (commitType) {
            case RepoUtils.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case RepoUtils.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case RepoUtils.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = mRepoUtils.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
        mCommitsListAdapter.resetCommit(mGit);
    }

    public void reset() {
        mCommitsListAdapter.resetCommit(mGit);
    }

    public void enterDiffActionMode() {
        mActionMode = getActivity().startActionMode(CommitsFragment.this);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.action_mode_commit_diff, menu);
        actionMode.setTitle(R.string.action_mode_diff);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_mode_diff:
                Integer[] items = mChosenItem.toArray(new Integer[0]);
                if (items.length == 0) {
                    mViewUtils.showToastMessage(R.string.alert_no_items_selected);
                    return true;
                }
                int item1, item2;
                item1 = items[0];
                if (items.length == 1) {
                    item2 = item1 + 1;
                    if (item2 == mCommitsListAdapter.getCount()) {
                        mViewUtils.showToastMessage(R.string.alert_no_older_commits);
                        return true;
                    }
                } else {
                    item2 = items[1];
                }
                Intent intent = new Intent(getActivity(), CommitDiffActivity.class);
                int smaller = Math.min(item1, item2);
                int larger = Math.max(item1, item2);
                String oldCommit = mCommitsListAdapter.getItem(larger).getName();
                String newCommit = mCommitsListAdapter.getItem(smaller).getName();
                intent.putExtra(CommitDiffActivity.OLD_COMMIT, oldCommit);
                intent.putExtra(CommitDiffActivity.NEW_COMMIT, newCommit);
                intent.putExtra(CommitDiffActivity.LOCAL_REPO, mLocalRepo);
                actionMode.finish();
                ActivityUtils.startActivity(getActivity(), intent);
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        mChosenItem.clear();
        mCommitsListAdapter.notifyDataSetChanged();
    }

    private void chooseItem(int position) {
        if (mChosenItem.contains(position)) {
            mChosenItem.remove(position);
            mCommitsListAdapter.notifyDataSetChanged();
            return;
        }
        if (mChosenItem.size() >= 2) {
            mViewUtils.showToastMessage(R.string.alert_choose_two_items);
            return;
        }
        mChosenItem.add(position);
        mCommitsListAdapter.notifyDataSetChanged();
    }

}
