package me.sheimi.android.views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.activities.SheimiFragmentActivity.OnPasswordEntered;

public class SheimiDialogFragment extends DialogFragment {

    private SheimiFragmentActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (SheimiFragmentActivity) context;
    }

    public SheimiFragmentActivity getRawActivity() {
        return mActivity;
    }

    public void showMessageDialog(int title, int msg, int positiveBtn,
            DialogInterface.OnClickListener positiveListener) {
        getRawActivity().showMessageDialog(title, msg, positiveBtn,
                positiveListener);
    }

    public void showMessageDialog(int title, String msg, int positiveBtn,
            DialogInterface.OnClickListener positiveListener) {
        getRawActivity().showMessageDialog(title, msg, positiveBtn,
                positiveListener);
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
