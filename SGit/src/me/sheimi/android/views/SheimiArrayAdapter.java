package me.sheimi.android.views;

import java.util.Collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;

public class SheimiArrayAdapter<T> extends ArrayAdapter<T> {

    public SheimiArrayAdapter(Context context, int resource) {
        super(context, resource);
    }
    
    @SuppressLint("NewApi")
    public void addAll(Collection<? extends T> collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.addAll(collection);
        } else {
            for (T item : collection) {
                add(item);
            }
        }
    }

    @SuppressLint("NewApi")
    public void adapterAddAll(T[] collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.addAll(collection);
        } else {
            for (T item : collection) {
                add(item);
            }
        }
    }

}
