package me.sheimi.sgit.activities.explorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

import java.io.File;
import java.io.FileOutputStream;

import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.ssh.PrivateKeyUtils;

public class PrivateKeyGenerate extends SheimiDialogFragment {

    private EditText mNewFilename;
    private EditText mKeyLength;
    private RadioButton mDSAButton;
    private RadioButton mRSAButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	LayoutInflater inflater = getActivity().getLayoutInflater();
	View view;
	view = inflater.inflate(R.layout.dialog_generate_key, null);
	mNewFilename = (EditText) view.findViewById(R.id.newFilename);
	mKeyLength = (EditText) view.findViewById(R.id.key_size);
	mKeyLength.setText("4096");
	mDSAButton = (RadioButton) view.findViewById(R.id.radio_dsa);
	mRSAButton = (RadioButton) view.findViewById(R.id.radio_rsa);
	mRSAButton.setChecked(true);
        builder.setMessage(R.string.label_dialog_generate_key)
	    .setView(view)
	    .setPositiveButton(R.string.label_generate_key, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			generateKey();
		    }
		})
	    .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			// Nothing to do
		    }
		});
        return builder.create();
    }

    private void generateKey() {
	String newFilename = mNewFilename.getText().toString().trim();

	if (newFilename.equals("")) {
            showToastMessage(R.string.alert_new_filename_required);
            mNewFilename
                    .setError(getString(R.string.alert_new_filename_required));
            return;
        }

        if (newFilename.contains("/")) {
            showToastMessage(R.string.alert_filename_format);
            mNewFilename.setError(getString(R.string.alert_filename_format));
            return;
        }

	int key_size = Integer.parseInt(mKeyLength.getText().toString());

	if (key_size < 1024) {
            showToastMessage(R.string.alert_too_short_key_size);
            mNewFilename.setError(getString(R.string.alert_too_short_key_size));
            return;
        }
	if (key_size > 16384) {
            showToastMessage(R.string.alert_too_long_key_size);
            mNewFilename.setError(getString(R.string.alert_too_long_key_size));
            return;
        }
	int type = mDSAButton.isChecked() ? KeyPair.DSA : KeyPair.RSA;
	File newKey = new File(PrivateKeyUtils.getPrivateKeyFolder(), newFilename);
	File newPubKey = new File(PrivateKeyUtils.getPublicKeyFolder(), newFilename);

	try {
	    JSch jsch=new JSch();
	    KeyPair kpair=KeyPair.genKeyPair(jsch, type, key_size);
	    kpair.writePrivateKey(new FileOutputStream(newKey));
	    kpair.writePublicKey(new FileOutputStream(newPubKey), "sgit");
	    kpair.dispose();
	} catch (Exception e) {
	    //TODO 
	    e.printStackTrace();
	}

		((PrivateKeyManageActivity)getActivity()).refreshList();
    }
}
