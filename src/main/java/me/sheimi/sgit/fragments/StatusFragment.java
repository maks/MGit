package me.sheimi.sgit.fragments;

import me.sheimi.android.activities.SheimiFragmentActivity.OnBackClickListener;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.StatusTask;
import me.sheimi.sgit.repo.tasks.repo.StatusTask.GetStatusCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by sheimi on 8/5/13.
 */
public class StatusFragment extends RepoDetailFragment {

    private Repo mRepo;
    private ProgressBar mLoadding;
    private TextView mStatus;

    public static StatusFragment newInstance(Repo mRepo) {
        StatusFragment fragment = new StatusFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        getRawActivity().setStatusFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mLoadding = (ProgressBar) v.findViewById(R.id.loading);
        mStatus = (TextView) v.findViewById(R.id.status);
        reset();
        return v;
    }

    @Override
    public void reset() {
        if (mLoadding == null || mStatus == null)
            return;
        mLoadding.setVisibility(View.VISIBLE);
        mStatus.setVisibility(View.GONE);
        StatusTask task = new StatusTask(mRepo, new GetStatusCallback() {
            @Override
            public void postStatus(String result) {
                mStatus.setText(result);
                mLoadding.setVisibility(View.GONE);
                mStatus.setVisibility(View.VISIBLE);
            }
        });
        task.executeTask();
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }
}
