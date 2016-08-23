package me.sheimi.sgit.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.sheimi.android.activities.SheimiFragmentActivity.OnBackClickListener;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.CommitDiffActivity;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.CommitsListAdapter;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.CheckoutDialog;

import org.eclipse.jgit.revwalk.RevCommit;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.SearchView;

/**
 * Created by sheimi on 8/5/13.
 */
public class CommitsFragment extends BaseFragment implements
        ActionMode.Callback {

    private final static String IS_ACTION_MODE = "is action mode";
    private final static String CHOSEN_ITEM = "chosen item";

    private ListView mCommitsList;
    private CommitsListAdapter mCommitsListAdapter;

    private ActionMode mActionMode;
    private Set<Integer> mChosenItem = new HashSet<Integer>();
    private Repo mRepo;
    private String mFile;
    private static final String FILE = "commit_file";

    private ClipboardManager mClipboard;

    public static CommitsFragment newInstance(Repo mRepo, String file) {
        CommitsFragment fragment = new CommitsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        if (file != null) {
            bundle.putString(FILE, file);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setFilter(String query){
        mCommitsListAdapter.setFilter(query);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_commits, container, false);
        if (getRawActivity() instanceof RepoDetailActivity) {
            ((RepoDetailActivity) getRawActivity()).setCommitsFragment(this);
        }

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null) {
            return v;
        }
        mFile = bundle.getString(FILE);
        mClipboard = (ClipboardManager) getRawActivity().getSystemService(
                Activity.CLIPBOARD_SERVICE);
        mCommitsList = (ListView) v.findViewById(R.id.commitsList);
        mCommitsListAdapter = new CommitsListAdapter(getRawActivity(),
                mChosenItem, mRepo, mFile);
        mCommitsListAdapter.resetCommit();
        mCommitsList.setAdapter(mCommitsListAdapter);

        mCommitsList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        if (mActionMode == null) {
                            RevCommit newCommit = mCommitsListAdapter.getItem(position);
                            showDiff(null, null, newCommit.getName(), true);
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
			if (mActionMode == null) {
                            enterDiffActionMode();
                        }
			chooseItem(position);
                        return true;
                    }
                });
        reset();
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
        outState.putBoolean(IS_ACTION_MODE, mActionMode != null);
        ArrayList<Integer> itemsList = new ArrayList<Integer>(mChosenItem);
        outState.putIntegerArrayList(CHOSEN_ITEM, itemsList);
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }

    @Override
    public void reset() {
        if (mCommitsListAdapter == null)
            return;
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

    private void showDiff(ActionMode actionMode, String oldCommit, String newCommit,
                          boolean showDescription) {
        Intent intent = new Intent(getRawActivity(),
                CommitDiffActivity.class);
        if (oldCommit != null) {
            intent.putExtra(CommitDiffActivity.OLD_COMMIT, oldCommit);
        }
        intent.putExtra(CommitDiffActivity.NEW_COMMIT, newCommit);
        intent.putExtra(CommitDiffActivity.SHOW_DESCRIPTION, showDescription);
        intent.putExtra(Repo.TAG, mRepo);
        if (actionMode != null) {
            actionMode.finish();
        }
        getRawActivity().startActivity(intent);
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

                int smaller = Math.min(item1, item2);
                int larger = Math.max(item1, item2);
                String oldCommit = mCommitsListAdapter.getItem(larger)
                        .getName();
                String newCommit = mCommitsListAdapter.getItem(smaller)
                        .getName();
                showDiff(actionMode, oldCommit, newCommit, false);
                return true;
            case R.id.action_mode_copy_commit: {
                if (mChosenItem.size() != 1) {
                    showToastMessage(R.string.alert_you_must_choose_one_commit_to_copy);
                    return true;
                }
                int item = mChosenItem.iterator().next();
                String commit = mCommitsListAdapter.getItem(item).getName();
                ClipData clip = ClipData.newPlainText("commit_to_copy", commit);
                mClipboard.setPrimaryClip(clip);
                showToastMessage(R.string.msg_commit_str_has_copied);
                actionMode.finish();
                return true;
            }
            case R.id.action_mode_checkout: {
                int item = mChosenItem.iterator().next();
                String commit = mCommitsListAdapter.getItem(item).getName();
                Bundle pathArg = new Bundle();
                pathArg.putString(CheckoutDialog.BASE_COMMIT, commit);
                pathArg.putSerializable(Repo.TAG, mRepo);
                actionMode.finish();
                CheckoutDialog ckd = new CheckoutDialog();
                ckd.setArguments(pathArg);
                ckd.show(getFragmentManager(), "rename-dialog");

                break;
            }

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
        if (mCommitsListAdapter.isProgressBar(position))
            return;
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
