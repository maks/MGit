package me.sheimi.sgit.adapters;

import me.sheimi.android.views.SheimiArrayAdapter;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.adapters.RepoOperationsAdapter.DrawerItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class RepoOperationsAdapter extends SheimiArrayAdapter<DrawerItem>
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
        public int name;
        public int icon;

        public DrawerItem(int name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private void setupDrawerItem() {
        for (int[] op : repoOps) {
            add(new DrawerItem(op[0], op[1]));
        }
    }

    private static final int[][] repoOps = { { R.string.drawer_file, 0 },
            { R.string.drawer_commit, 0 },
            { R.string.action_merge, R.drawable.ic_merge },
            { R.string.action_pull, R.drawable.ic_download },
            { R.string.action_push, R.drawable.ic_push },
            { R.string.action_diff, R.drawable.ic_file },
            { R.string.action_commit, 0 }, { R.string.action_reset, 0 },
            { R.string.action_new_file, 0 }, { R.string.action_new_dir, 0 },
            { R.string.action_delete, 0 }, };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        RepoDetailActivity context = (RepoDetailActivity) getContext();
        DrawerItem item = getItem(position);
        switch (item.name) {
            case R.string.drawer_file:
            case R.string.drawer_commit:
                context.selectFragment(position);
                break;
            default:
                context.selectRepoOperation(item.name);
                break;
        }

    }
}
