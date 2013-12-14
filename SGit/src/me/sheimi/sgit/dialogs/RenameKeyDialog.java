package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.explorer.PrivateKeyManageActivity;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/24/13.
 */

public class RenameKeyDialog extends DialogFragment implements
        View.OnClickListener, DialogInterface.OnClickListener {

    private File mFromFile;
    private String mFromPath;
    private EditText mNewFilename;
    private ViewUtils mViewUtils;
    private PrivateKeyManageActivity mActivity;
    private static final String FROM_PATH = "from path";

    public RenameKeyDialog() {
    }

    public RenameKeyDialog(String fromPath) {
        mFromPath = fromPath;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (PrivateKeyManageActivity) getActivity();
        mViewUtils = ViewUtils.getInstance(mActivity);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        if (savedInstanceState != null) {
            String fromPath = savedInstanceState.getString(FROM_PATH);
            if (fromPath != null) {
                mFromPath = fromPath;
            }
        }
        mFromFile = new File(mFromPath);

        builder.setTitle(getString(R.string.dialog_rename_key_title));
        View view = mActivity.getLayoutInflater().inflate(
                R.layout.dialog_rename_key, null);

        builder.setView(view);
        mNewFilename = (EditText) view.findViewById(R.id.newFilename);
        mNewFilename.setText(mFromFile.getName());

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setNeutralButton(R.string.label_delete, this);
        builder.setPositiveButton(R.string.label_rename,
                new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FROM_PATH, mFromPath);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = (Button) dialog
                .getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String newFilename = mNewFilename.getText().toString().trim();
        if (newFilename.equals("")) {
            mViewUtils.showToastMessage(R.string.alert_new_filename_required);
            mNewFilename
                    .setError(getString(R.string.alert_new_filename_required));
            return;
        }

        if (newFilename.contains("/")) {
            mViewUtils.showToastMessage(R.string.alert_filename_format);
            mNewFilename.setError(getString(R.string.alert_filename_format));
            return;
        }

        File file = new File(mFromFile.getParentFile(), newFilename);
        if (file.exists()) {
            mViewUtils.showToastMessage(R.string.alert_file_exists);
            mNewFilename.setError(getString(R.string.alert_file_exists));
            return;
        }
        mFromFile.renameTo(file);
        mActivity.refreshList();
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        FsUtils.getInstance(mActivity).deleteFile(mFromFile);
        mActivity.refreshList();
    }

}