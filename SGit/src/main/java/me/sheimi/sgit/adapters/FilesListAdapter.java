package me.sheimi.sgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import me.sheimi.sgit.R;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/18/13.
 */
public class FilesListAdapter extends ArrayAdapter<File> {

    private File mDir;
    private ViewUtils mViewUtils;
    private FileFilter mFileFilter;

    public FilesListAdapter(Context context, FileFilter fileFilter) {
        super(context, 0);
        mViewUtils = ViewUtils.getInstance(context);
        mFileFilter = fileFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        FilesListItemHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_files,
                    parent, false);
            holder = new FilesListItemHolder();
            holder.fileTitle = (TextView) convertView.findViewById(R.id
                    .fileTitle);
            holder.fileIcon = (ImageView) convertView.findViewById(R.id
                    .fileIcon);
            convertView.setTag(holder);
        } else {
            holder = (FilesListItemHolder) convertView.getTag();
        }
        File item = getItem(position);
        holder.fileTitle.setText(item.getName());
        if (item.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder_d);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file_d);
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
                if ((!file1.isDirectory() && !file2.isDirectory()
                        || (file1.isDirectory() && file2.isDirectory()))) {
                    return file1.toString().compareTo(file2.toString());
                }
                return file1.isDirectory() ? -1 : 1;
            }
        });
        mViewUtils.adapterAddAll(this, files);
        notifyDataSetChanged();;
    }

    private static class FilesListItemHolder {
        public TextView fileTitle;
        public ImageView fileIcon;
    }
}
