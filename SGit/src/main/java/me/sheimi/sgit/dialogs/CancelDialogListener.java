package me.sheimi.sgit.dialogs;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

/**
 * Created by sheimi on 8/20/13.
 */
public class CancelDialogListener implements DialogInterface
        .OnClickListener {

    private DialogFragment mDF;

    public CancelDialogListener(DialogFragment df) {
        mDF = df;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        mDF.getDialog().cancel();
    }
}