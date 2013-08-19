package me.sheimi.sgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.sheimi.sgit.R;
import me.sheimi.sgit.utils.CommonUtils;
import me.sheimi.sgit.utils.ImageCache;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/18/13.
 */
public class CommitsListAdapter extends ArrayAdapter<RevCommit> {

    private File mDir;
    private RepoUtils mRepoUtils;
    private static final SimpleDateFormat COMMITTIME_FORMATTER = new
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private static final String IMAGE_REQUEST_HASH =
            "http://www.gravatar.com/avatar/%s?s=40";

    public CommitsListAdapter(Context context) {
        super(context, 0);
        mRepoUtils = RepoUtils.getInstance(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        CommitsListItemHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_commits,
                    parent, false);
            holder = new CommitsListItemHolder();
            holder.commitsTitle = (TextView) convertView.findViewById(R.id
                    .commitTitle);
            holder.commitsIcon = (ImageView) convertView.findViewById(R.id
                    .commitIcon);
            holder.commitAuthor = (TextView) convertView.findViewById(R.id
                    .commitAuthor);
            holder.commitsMsg = (TextView) convertView.findViewById(R.id
                    .commitMsg);
            holder.commitTime = (TextView) convertView.findViewById(R.id
                    .commitTime);
            convertView.setTag(holder);
        } else {
            holder = (CommitsListItemHolder) convertView.getTag();
        }
        RevCommit commit = getItem(position);
        PersonIdent person = commit.getCommitterIdent();
        Date date = person.getWhen();
        String email = person.getEmailAddress();

        holder.commitsTitle.setText(mRepoUtils.getShortenCommitName(commit));
        holder.commitAuthor.setText(person.getName());
        holder.commitsMsg.setText(commit.getShortMessage());
        holder.commitTime.setText(COMMITTIME_FORMATTER.format(date));
        holder.commitsIcon.setImageResource(R.drawable.ic_default_author);

        ImageLoader im = ImageCache.getInstance(getContext()).getImageLoader();
        im.displayImage(buildIconURL(email), holder.commitsIcon);
        return convertView;
    }

    public void resetCommit(Git git) {
        clear();
        List<RevCommit> commitsList = mRepoUtils.getCommitsList(git);
        addAll(commitsList);
        notifyDataSetChanged();
    }

    private String buildIconURL(String email) {
        String hash = CommonUtils.md5(email);
        String url = String.format(Locale.getDefault(), IMAGE_REQUEST_HASH,
                hash);
        return url;
    }

    private static class CommitsListItemHolder {
        public ImageView commitsIcon;
        public TextView commitsTitle;
        public TextView commitsMsg;
        public TextView commitAuthor;
        public TextView commitTime;
    }
}
