package me.sheimi.sgit.fragments;

import me.sheimi.sgit.activities.RepoDetailActivity;

public abstract class RepoDetailFragment extends BaseFragment {

    public RepoDetailActivity getRawActivity() {
        return (RepoDetailActivity) super.getRawActivity();
    }
    
}
