package me.sheimi.sgit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.SGitApplication;
import me.sheimi.sgit.activities.explorer.PrivateKeyManageActivity;
import me.sheimi.sgit.ssh.PrivateKeyUtils;
import timber.log.Timber;

/**
 * Allowing editing password for a stored private key
 */

public class EditKeyPasswordDialog extends SheimiDialogFragment implements
        View.OnClickListener, DialogInterface.OnClickListener {

    private File mKeyFile;
    private PrivateKeyManageActivity mActivity;
    public static final String KEY_FILE_EXTRA = "extra_key_file";
    private EditText mPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (PrivateKeyManageActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_FILE_EXTRA)) {
            mKeyFile = new File(args.getString(KEY_FILE_EXTRA));
        }

        builder.setTitle(getString(R.string.dialog_edit_key_password_title));
        View view = mActivity.getLayoutInflater().inflate(
                R.layout.dialog_prompt_for_password_only, null);

        builder.setView(view);
        mPassword = (EditText) view.findViewById(R.id.password);

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_save,
                new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FILE_EXTRA, mKeyFile.getAbsolutePath());
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }
        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String newPassword = mPassword.getText().toString().trim();
        try {
            ((SGitApplication)getActivity().getApplicationContext()).getSecurePrefsHelper().
                set(mKeyFile.getName(), newPassword);
        } catch (Exception e) {
            Timber.e(e);
        }
        mActivity.refreshList();
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
    }

}
