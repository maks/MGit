package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;

/**
 * Created by sheimi on 8/16/13.
 */
public class CommitDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private RepoDetailActivity mActivity;
    private TextView mCommitDialogMsg;
    private EditText mCommitMsg;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (RepoDetailActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_commit, null);
        mCommitDialogMsg = (TextView) layout.findViewById(R.id.commitDialogMsg);
        mCommitMsg = (EditText) layout.findViewById(R.id.commitMsg);
        mCommitDialogMsg.setText(getString(R.string.dialog_commit_dialog_msg));
        builder.setTitle(R.string.dialog_commit_title);
        builder.setView(layout);

        // set button listener
        builder.setNegativeButton(R.string.label_cancel, new DummyDialogListener());
        builder.setPositiveButton(R.string.label_commit, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        mActivity.commitChanges(mCommitMsg.getText().toString());
    }
}
