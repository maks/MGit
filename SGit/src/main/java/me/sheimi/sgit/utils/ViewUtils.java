package me.sheimi.sgit.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collection;

import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.DummyDialogListener;

/**
 * Created by sheimi on 8/22/13.
 */
public class ViewUtils {

    private static ViewUtils mInstance;

    Context mContext;

    private ViewUtils(Context context) {
        mContext = context;
    }

    public static ViewUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ViewUtils(context);
        }
        if (context != null) {
            mInstance.mContext = context;
        }
        return mInstance;
    }

    public void showToastMessage(final String msg) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void showToastMessage(int resId) {
        showToastMessage(mContext.getString(resId));
    }

    public int getColor(int resId) {
        return mContext.getResources().getColor(resId);
    }

    public <T> void adapterAddAll(ArrayAdapter<T> adapter, Collection<T> collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            adapter.addAll(collection);
        } else {
            for (T item : collection) {
                adapter.add(item);
            }
        }
    }

    public <T> void adapterAddAll(ArrayAdapter<T> adapter, T[] collection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            adapter.addAll(collection);
        } else {
            for (T item : collection) {
                adapter.add(item);
            }
        }
    }

    public void showMessageDialog(int title, int msg, int positiveBtn,
                                  DialogInterface.OnClickListener positiveListenerr) {
        showMessageDialog(title, msg, positiveBtn, R.string.label_cancel,
                positiveListenerr, new DummyDialogListener());
    }

    public void showMessageDialog(int title, int msg, int positiveBtn, int negativeBtn,
                                  DialogInterface.OnClickListener positiveListener,
                                  DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title).setMessage(msg)
                .setPositiveButton(positiveBtn, positiveListener)
                .setNegativeButton(negativeBtn, negativeListener).show();
    }

    public void showEditTextDialog(int title, int hint, int positiveBtn,
                                   final OnEditTextDialogClicked positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText editText = (EditText) layout.findViewById(R.id.editText);
        editText.setHint(hint);
        builder.setTitle(title).setView(layout)
                .setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        positiveListener.onClicked(editText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DummyDialogListener()).show();
    }

    public static interface OnEditTextDialogClicked {
        void onClicked(String text);
    }

}
