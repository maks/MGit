package me.sheimi.sgit.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.sheimi.android.activities.SheimiFragmentActivity.OnBackClickListener;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.CommitDiffActivity;
import me.sheimi.sgit.adapters.CommitsListAdapter;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.ChooseCommitDialog;

import org.eclipse.jgit.revwalk.RevCommit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Created by sheimi on 8/5/13.
 */
public class CommitsFragment extends RepoDetailFragment implements
        ActionMode.Callback {

    private final static String IS_ACTION_MODE = "is action mode";
    private final static String CHOSEN_ITEM = "chosen item";

    private Button mCommitNameButton;
    private ImageView mCommitType;
    private ListView mCommitsList;
    private CommitsListAdapter mCommitsListAdapter;

    private ActionMode mActionMode;
    private Set<Integer> mChosenItem = new HashSet<Integer>();
    private Repo mRepo;

    public static CommitsFragment newInstance(Repo mRepo) {
        CommitsFragment fragment = new CommitsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_commits, container, false);
        getRawActivity().setCommitsFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mRepo.setContext(getRawActivity());

        mCommitsList = (ListView) v.findViewById(R.id.commitsList);
        mCommitNameButton = (Button) v.findViewById(R.id.commitName);
        mCommitType = (ImageView) v.findViewById(R.id.commitType);
        mCommitsListAdapter = new CommitsListAdapter(getRawActivity(),
                mChosenItem, mRepo);
        mCommitsListAdapter.resetCommit();
        mCommitsList.setAdapter(mCommitsListAdapter);

        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCommitDialog cbd = new ChooseCommitDialog(mRepo);
                cbd.show(getFragmentManager(), "choose-branch-dialog");
            }
        });
        mCommitsList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        if (mActionMode == null) {
                            RevCommit commit = mCommitsListAdapter
                                    .getItem(position);
                            final String fullCommitName = commit.getName();
                            String message = getString(R.string.dialog_comfirm_checkout_commit_msg)
                                    + " "
                                    + Repo.getCommitDisplayName(fullCommitName);
                            showMessageDialog(
                                    R.string.dialog_comfirm_checkout_commit_title,
                                    message, R.string.label_checkout,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialogInterface,
                                                int i) {
                                            getRawActivity().resetCommits(
                                                    fullCommitName);
                                        }
                                    });
                            return;
                        }
                        chooseItem(position);
                    }
                });
        mCommitsList
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView,
                            View view, int position, long l) {
                        if (mActionMode != null) {
                            return false;
                        }
                        enterDiffActionMode();
                        chooseItem(position);
                        return true;
                    }
                });

        String branchName = mRepo.getBranchName();
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
            List<Integer> itemsInt = savedInstanceState
                    .getIntegerArrayList(CHOSEN_ITEM);
            mActionMode = getRawActivity().startActionMode(this);
            mChosenItem.addAll(itemsInt);
            mCommitsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Repo.TAG, mRepo);
        outState.putBoolean(IS_ACTION_MODE, mActionMode != null);
        ArrayList<Integer> itemsList = new ArrayList<Integer>(mChosenItem);
        outState.putIntegerArrayList(CHOSEN_ITEM, itemsList);
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }

    public void reset(String commitName) {
        int commitType = Repo.getCommitType(commitName);
        switch (commitType) {
            case Repo.COMMIT_TYPE_REMOTE:
                // change the display name to local branch
                commitName = Repo.convertRemoteName(commitName);
            case Repo.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case Repo.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case Repo.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = Repo.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
        mCommitsListAdapter.resetCommit();
    }

    public void reset() {
        mCommitsListAdapter.resetCommit();
    }

    public void enterDiffActionMode() {
        mActionMode = getRawActivity().startActionMode(CommitsFragment.this);
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
                    showToastMessage(R.string.alert_no_items_selected);
                    return true;
                }
                int item1,
                item2;
                item1 = items[0];
                if (items.length == 1) {
                    item2 = item1 + 1;
                    if (item2 == mCommitsListAdapter.getCount()) {
                        showToastMessage(R.string.alert_no_older_commits);
                        return true;
                    }
                } else {
                    item2 = items[1];
                }
                Intent intent = new Intent(getRawActivity(),
                        CommitDiffActivity.class);
                int smaller = Math.min(item1, item2);
                int larger = Math.max(item1, item2);
                String oldCommit = mCommitsListAdapter.getItem(larger)
                        .getName();
                String newCommit = mCommitsListAdapter.getItem(smaller)
                        .getName();
                intent.putExtra(CommitDiffActivity.OLD_COMMIT, oldCommit);
                intent.putExtra(CommitDiffActivity.NEW_COMMIT, newCommit);
                intent.putExtra(Repo.TAG, mRepo);
                actionMode.finish();
                getRawActivity().startActivity(intent);
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
            showToastMessage(R.string.alert_choose_two_items);
            return;
        }
        mChosenItem.add(position);
        mCommitsListAdapter.notifyDataSetChanged();
    }

}
