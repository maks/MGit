package me.sheimi.sgit.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.GetCommitTask;
import me.sheimi.sgit.repo.tasks.repo.GetCommitTask.GetCommitCallback;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by sheimi on 8/18/13.
 */
public class CommitsListAdapter extends BaseAdapter {

    private Repo mRepo;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new SimpleDateFormat(
            "MM/dd/yyyy", Locale.getDefault());
    private Set<Integer> mChosenItems;
    private String mFilter;
    private ArrayList<RevCommit> mAll;
    private ArrayList<Integer> mFiltered;
    private Context mContext;
    private String mFile;

    public CommitsListAdapter(Context context, Set<Integer> chosenItems,
                              Repo repo, String file) {
        super();
        mFile = file;
        mContext = context;
        mChosenItems = chosenItems;
        mRepo = repo;
        mAll = new ArrayList<RevCommit>();
        mFiltered = null;
        mFilter = null;
    }

    private boolean isAccepted(RevCommit in) {
        if (mFilter == null) {
            return true;
        }
        return (in.getId().toString().startsWith("commit " + mFilter.toLowerCase())
                || new String(in.getRawBuffer()).contains(mFilter));
    }

    private void doFiltering() {
        if (mFilter == null) {
            mFiltered = null;
        } else {
            mFiltered = new ArrayList<>();
            int i;
            for (i = 0; i < mAll.size(); i++)
                if (isAccepted(mAll.get(i)))
                    mFiltered.add(i);
        }
    }

    public void setFilter(String query) {
        if (query == null || query.equals("")) {
            mFilter = null;
        } else {
            mFilter = query;
        }
        doFiltering();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mFilter == null) ? mAll.size() : mFiltered.size();
    }

    @Override
    public long getItemId(int position) {
        if (mFilter == null) {
            return position;
        } else {
            return mFiltered.get(position);
        }
    }

    public RevCommit getItem(int position) {
        return (mFilter == null) ? mAll.get(position) : mAll.get(mFiltered.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        CommitsListItemHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_commits, parent,
                    false);
            holder = new CommitsListItemHolder();
            holder.commitsTitle = (TextView) convertView
                    .findViewById(R.id.commitTitle);
            holder.commitsIcon = (ImageView) convertView
                    .findViewById(R.id.commitIcon);
            holder.commitAuthor = (TextView) convertView
                    .findViewById(R.id.commitAuthor);
            holder.commitsMsg = (TextView) convertView
                    .findViewById(R.id.commitMsg);
            holder.commitTime = (TextView) convertView
                    .findViewById(R.id.commitTime);
            convertView.setTag(holder);
        } else {
            holder = (CommitsListItemHolder) convertView.getTag();
        }
        RevCommit commit = getItem(position);
        PersonIdent person = commit.getAuthorIdent();
        Date date = person.getWhen();
        String email = person.getEmailAddress();

        holder.commitsTitle
                .setText(Repo.getCommitDisplayName(commit.getName()));
        holder.commitAuthor.setText(person.getName());
        holder.commitsMsg.setText(commit.getShortMessage());
        holder.commitTime.setText(COMMITTIME_FORMATTER.format(date));
        holder.commitsIcon.setImageResource(R.drawable.ic_default_author);

        ImageLoader im = BasicFunctions.getImageLoader();
        im.displayImage(BasicFunctions.buildGravatarURL(email),
                holder.commitsIcon);

        int color, colorResId;
        if (mChosenItems.contains(position)) {
            colorResId = R.color.pressed_sgit;
        } else {
            colorResId = android.R.color.transparent;
        }
        if (mContext instanceof SheimiFragmentActivity) {
            color = mContext.getResources().getColor(colorResId);
            convertView.setBackgroundColor(color);
        }
        return convertView;
    }

    public void clear() {
        mAll = new ArrayList<>();
        if (mFilter == null) {
            mFiltered = null;
        } else {
            mFiltered = new ArrayList<>();
        }
    }

    public void resetCommit() {
        clear();
        GetCommitTask getCommitTask = new GetCommitTask(mRepo, mFile,
                new GetCommitCallback() {

                    @Override
                    public void postCommits(List<RevCommit> commits) {
                        if (commits != null) {
                            // TODO why == null
                            mAll = new ArrayList<>(commits);
                            doFiltering();
                        }
                        notifyDataSetChanged();
                    }
                });
        getCommitTask.executeTask();
    }

    private static class CommitsListItemHolder {
        public ImageView commitsIcon;
        public TextView commitsTitle;
        public TextView commitsMsg;
        public TextView commitAuthor;
        public TextView commitTime;
    }
}
