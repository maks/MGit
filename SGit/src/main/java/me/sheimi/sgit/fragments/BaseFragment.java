package me.sheimi.sgit.fragments;

import android.support.v4.app.Fragment;

import me.sheimi.sgit.listeners.OnBackClickListener;

/**
 * Created by sheimi on 8/7/13.
 */
public abstract class BaseFragment extends Fragment {

    public abstract OnBackClickListener getOnBackClickListener();

	// public abstract void search(String query);

	// public abstract void reset();

}
