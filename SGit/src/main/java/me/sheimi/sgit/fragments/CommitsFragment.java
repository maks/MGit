package me.sheimi.sgit.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by sheimi on 8/5/13.
 */
public class CommitsFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout rl = new RelativeLayout(getActivity());
		TextView tv = new TextView(getActivity());
		tv.setText("Picture Fragment");
		rl.addView(tv);
		return rl;
	}

}
