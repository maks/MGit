package me.sheimi.sgit.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.CommitsListAdapter;
import me.sheimi.sgit.dialogs.ChooseCommitDialog;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/5/13.
 */
public class CommitsFragment extends BaseFragment {

    private RepoDetailActivity mActivity;
    private Button mCommitNameButton;
    private ImageView mCommitType;
    private ListView mCommitsList;
    private CommitsListAdapter mCommitsListAdapter;

    private RepoUtils mRepoUtils;
    private Git mGit;


    public CommitsFragment(Git git) {
        mGit = git;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_commits, container, false);
        mRepoUtils = RepoUtils.getInstance(mActivity);
        mActivity = (RepoDetailActivity) getActivity();
        mCommitsList = (ListView) v.findViewById(R.id.commitsList);
        mCommitNameButton = (Button) v.findViewById(R.id.commitName);
        mCommitType = (ImageView) v.findViewById(R.id.commitType);
        mCommitsListAdapter = new CommitsListAdapter(mActivity);
        mCommitsListAdapter.resetCommit(mGit);
        mCommitsList.setAdapter(mCommitsListAdapter);

        mCommitsList.setOnItemClickListener(new AdapterView
                .OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                RevCommit commit = mCommitsListAdapter.getItem(position);
                String fullCommitName = commit.getName();
                mActivity.resetCommits(fullCommitName);
            }
        });
        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCommitDialog cbd = new ChooseCommitDialog(mGit);
                cbd.show(getFragmentManager(), "choose-branch-dialog");
            }
        });

        String branchName = mRepoUtils.getBranchName(mGit);
        reset(branchName);
        return v;
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
}
