package me.sheimi.sgit.activities;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.delegate.RepoOperationDelegate;
import me.sheimi.sgit.adapters.RepoOperationsAdapter;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.fragments.BaseFragment;
import me.sheimi.sgit.fragments.CommitsFragment;
import me.sheimi.sgit.fragments.FilesFragment;
import me.sheimi.sgit.fragments.StatusFragment;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskCallback;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import me.sheimi.sgit.activities.delegate.RepoOperationDelegate;
import me.sheimi.sgit.repo.tasks.repo.CheckoutTask;
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask.AsyncTaskPostCallback;

public class BranchChooserActivity extends Activity {
    private Repo mRepo;
    private ListView mBranchTagList;
    private ProgressBar mLoadding;
    private BranchTagListAdapter mAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    View v = getLayoutInflater().inflate(R.layout.fragment_branches, null);
        mRepo = (Repo) getIntent().getSerializableExtra(Repo.TAG);
        mBranchTagList = (ListView) v.findViewById(R.id.branches);
        mLoadding = (ProgressBar) v.findViewById(R.id.loading);
        mAdapter = new BranchTagListAdapter(this);
        mBranchTagList.setAdapter(mAdapter);

	setTitle(R.string.dialog_choose_branch_title);

	String[] branches = mRepo.getBranches();
        String[] tags = mRepo.getTags();
        mAdapter.addAll(branches);
        mAdapter.addAll(tags);

        mBranchTagList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                                            View view, int position, long id) {
                        String commitName = mAdapter.getItem(position);
                        CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName, null,
                                new AsyncTaskPostCallback() {
                                    @Override
                                    public void onPostExecute(Boolean isSuccess) {
                                        finish();
                                    }
                                });
                        mLoadding.setVisibility(View.VISIBLE);
                        mBranchTagList.setVisibility(View.GONE);
                        checkoutTask.executeTask();
                    }
                });

        setContentView(v);
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
