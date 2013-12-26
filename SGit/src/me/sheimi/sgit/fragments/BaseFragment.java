package me.sheimi.sgit.fragments;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.activities.SheimiFragmentActivity.OnBackClickListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;

/**
 * Created by sheimi on 8/7/13.
 */
public abstract class BaseFragment extends Fragment {

    public abstract OnBackClickListener getOnBackClickListener();

    private SheimiFragmentActivity mActivity;

    public abstract void reset();

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (SheimiFragmentActivity) activity;
    }

    public SheimiFragmentActivity getRawActivity() {
        return mActivity;
    }

    public void showMessageDialog(int title, int msg, int positiveBtn,
            DialogInterface.OnClickListener positiveListenerr) {
        getRawActivity().showMessageDialog(title, msg, positiveBtn,
                positiveListenerr);
    }

    public void showMessageDialog(int title, String msg, int positiveBtn,
            DialogInterface.OnClickListener positiveListenerr) {
        getRawActivity().showMessageDialog(title, msg, positiveBtn,
                positiveListenerr);
    }

    public void showToastMessage(int resId) {
        getRawActivity().showToastMessage(getString(resId));
    }

    public void showToastMessage(String msg) {
        getRawActivity().showToastMessage(msg);
    }

    // public abstract void search(String query);
}
