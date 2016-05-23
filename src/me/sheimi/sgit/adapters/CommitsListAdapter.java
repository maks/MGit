package me.sheimi.sgit.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.views.SheimiArrayAdapter;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by sheimi on 8/18/13.
 */
public class CommitsListAdapter extends SheimiArrayAdapter<RevCommit> {

    private Repo mRepo;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new SimpleDateFormat(
            "MM/dd/yyyy", Locale.getDefault());
    private Set<Integer> mChosenItems;

    public CommitsListAdapter(Context context, Set<Integer> chosenItems,
            Repo repo) {
        super(context, 0);
        mChosenItems = chosenItems;
        mRepo = repo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
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
        PersonIdent person = commit.getCommitterIdent();
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

        if (mChosenItems.contains(position)) {
            convertView.setBackgroundColor(getColor(R.color.pressed_sgit));
        } else {
            convertView
                    .setBackgroundColor(getColor(android.R.color.transparent));
        }

        return convertView;
    }

    private int getColor(int resId) {
        Context context = getContext();
        if (context instanceof SheimiFragmentActivity) {
            return ((SheimiFragmentActivity) context).getResColor(resId);
        }
        return 0;
    }

    public void resetCommit() {
        clear();
        GetCommitTask getCommitTask = new GetCommitTask(mRepo,
                new GetCommitCallback() {

                    @Override
                    public void postCommits(List<RevCommit> commits) {
                        // TODO Auto-generated method stub
                        if (commits != null) {
                            // TODO why == null
                        	clear();
                            addAll(commits);
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
