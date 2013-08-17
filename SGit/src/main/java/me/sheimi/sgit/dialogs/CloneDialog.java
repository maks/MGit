package me.sheimi.sgit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.RepoContract;
import me.sheimi.sgit.database.RepoDbManager;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/16/13.
 */
public class CloneDialog extends DialogFragment {

    private EditText mRemoteURL;
    private EditText mLocalPath;
    private Activity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_clone, null);
        builder.setView(layout);

        mRemoteURL = (EditText) layout.findViewById(R.id.remoteURL);
        mLocalPath = (EditText) layout.findViewById(R.id.localPath);

        mRemoteURL.setText(RepoUtils.TEST_REPO);

        // set button listener
        builder.setNegativeButton(getString(R.string.label_cancel),
                new CancelDialogListener());
        builder.setPositiveButton(getString(R.string.label_clone),
                new OnCloneClickedListener());

        return builder.create();
    }

    private class CancelDialogListener implements DialogInterface
            .OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            getDialog().cancel();
        }
    }

    private class OnCloneClickedListener implements DialogInterface
            .OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String remoteURL = mRemoteURL.getText().toString();
                    final String localPath = mLocalPath.getText().toString();

                    RepoUtils.getInstance(mActivity).cloneSync(remoteURL,
                            localPath);
                    ContentValues values = new ContentValues();
                    values.put(RepoContract.RepoEntry
                            .COLUMN_NAME_LOCAL_PATH,
                            localPath);
                    values.put(RepoContract.RepoEntry
                            .COLUMN_NAME_REMOTE_URL,
                            remoteURL);
                    RepoDbManager.getInstance(mActivity).insertRepo
                            (values);
                }
            });
            thread.start();
        }
    }

}
