package me.sheimi.android.views;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;

public class SheimiDialogFragment extends DialogFragment {

    private SheimiFragmentActivity mActivity;

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

    public void promptForPassword(OnPasswordEntered onPasswordEntered,
            int errorId) {
        getRawActivity().promptForPassword(onPasswordEntered, errorId);
    }

    public void promptForPassword(OnPasswordEntered onPasswordEntered) {
        getRawActivity().promptForPassword(onPasswordEntered, null);
    }
}
