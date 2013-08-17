package me.sheimi.sgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;

import me.sheimi.sgit.R;

/**
 * Created by sheimi on 8/18/13.
 */
public class FilesListAdapter extends ArrayAdapter<File> {

    private File mDir;

    public FilesListAdapter(Context context) {
        super(context, 0);
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
            convertView.setTag(holder);
        } else {
            holder = (FilesListItemHolder) convertView.getTag();
        }
        File item = getItem(position);
        holder.fileTitle.setText(item.getName());
        return convertView;
    }

    public void setDir(File dir) {
        mDir = dir;
        clear();
        addAll(dir.listFiles());
        notifyDataSetChanged();;
    }

    private static class FilesListItemHolder {
        public TextView fileTitle;
    }
}
