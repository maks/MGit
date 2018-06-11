package me.sheimi.sgit.adapters;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import me.sheimi.android.utils.Profile;
import me.sheimi.sgit.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

/**
 * Created by sheimi on 8/18/13.
 */
public class FilesListAdapter extends ArrayAdapter<File> {

    private File mDir;
    private FileFilter mFileFilter;

    public FilesListAdapter(Context context, FileFilter fileFilter) {
        super(context, 0);
        mFileFilter = fileFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        FilesListItemHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_files, parent,
                    false);
            holder = new FilesListItemHolder();
            holder.fileTitle = (TextView) convertView
                    .findViewById(R.id.fileTitle);
            holder.fileIcon = (ImageView) convertView
                    .findViewById(R.id.fileIcon);
            convertView.setTag(holder);
        } else {
            holder = (FilesListItemHolder) convertView.getTag();
        }
        File item = getItem(position);
        holder.fileTitle.setText(item.getName());
        if (item.isDirectory()) {
            holder.fileIcon.setImageResource(Profile.getStyledResource(getContext(), R.attr.ic_folder_fl));
        } else {
            holder.fileIcon.setImageResource(Profile.getStyledResource(getContext(), R.attr.ic_file_fl));
        }
        // set if selected
        if (convertView.isSelected()) {
            convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.pressed_sgit));
        } else {
            convertView.setBackgroundColor(convertView.getContext().getResources().getColor(android.R.color.transparent));
        }
        return convertView;
    }

    public void setDir(File dir) {
        mDir = dir;
        clear();
        File[] files = null;
        if (mFileFilter == null) {
            files = dir.listFiles();
        } else {
            files = dir.listFiles(mFileFilter);
        }
        // this is to fix a bug
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                // if file1 and file2 are the same type (dir or file)
                if ((!file1.isDirectory() && !file2.isDirectory() || (file1
                        .isDirectory() && file2.isDirectory()))) {
                    return file1.toString().compareTo(file2.toString());
                }
                return file1.isDirectory() ? -1 : 1;
            }
        });
        addAll(files);
        notifyDataSetChanged();
    }

    private static class FilesListItemHolder {
        public TextView fileTitle;
        public ImageView fileIcon;
    }
}
