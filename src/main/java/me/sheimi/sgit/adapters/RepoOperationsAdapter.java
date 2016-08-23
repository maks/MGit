package me.sheimi.sgit.adapters;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.RepoOperationsAdapter.DrawerItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RepoOperationsAdapter extends ArrayAdapter<DrawerItem>
        implements OnItemClickListener {

    public RepoOperationsAdapter(Context context) {
        super(context, 0);
        setupDrawerItem();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(getContext(), parent);
        }
        bindView(convertView, position);
        return convertView;
    }

    public View newView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.drawer_list_item, parent, false);
        DrawerItemHolder holder = new DrawerItemHolder();
        holder.name = (TextView) view.findViewById(R.id.name);
        view.setTag(holder);
        return view;
    }

    public void bindView(View view, int position) {
        DrawerItemHolder holder = (DrawerItemHolder) view.getTag();
        DrawerItem item = getItem(position);
        holder.name.setText(item.name);
    }

    public static class DrawerItemHolder {
        public TextView name;
    }

    public static class DrawerItem {
        public String name;
        public int icon;

        public DrawerItem(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private void setupDrawerItem() {
        String[] ops = getContext().getResources().getStringArray(
                R.array.repo_operation_names);
        for (String op : ops) {
            add(new DrawerItem(op, 0));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        RepoDetailActivity context = (RepoDetailActivity) getContext();
        context.getRepoDelegate().executeAction(position);
    }

}
