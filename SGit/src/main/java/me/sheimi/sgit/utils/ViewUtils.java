package me.sheimi.sgit.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.DummyDialogListener;

/**
 * Created by sheimi on 8/22/13.
 */
public class ViewUtils {

    private static Map<Context, ViewUtils> mInstances = new HashMap<Context, ViewUtils>();

    Context mContext;

    private ViewUtils(Context context) {
        mContext = context;
    }

    public static ViewUtils getInstance(Context context) {
        ViewUtils viewUtils = mInstances.get(context);
        if (viewUtils == null) {
            viewUtils = new ViewUtils(context);
            mInstances.put(context, viewUtils);
        }
        return viewUtils;
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
        showMessageDialog(title, mContext.getString(msg), positiveBtn, R.string.label_cancel,
                positiveListenerr, new DummyDialogListener());
    }

    public void showMessageDialog(int title, String msg, int positiveBtn,
                                  DialogInterface.OnClickListener positiveListenerr) {
        showMessageDialog(title, msg, positiveBtn, R.string.label_cancel,
                positiveListenerr, new DummyDialogListener());
    }

    public void showMessageDialog(int title, String msg, int positiveBtn, int negativeBtn,
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

    public void promptForPassword(final OnPasswordEntered onPasswordEntered, String errorInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_prompt_for_password, null);
        final EditText username = (EditText) layout.findViewById(R.id.username);
        final EditText password = (EditText) layout.findViewById(R.id.password);
        final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.savePassword);
        if (errorInfo == null) {
            errorInfo = mContext.getString(R.string.dialog_prompt_for_password_title);
        }
        builder.setTitle(errorInfo).setView(layout)
                .setPositiveButton(R.string.label_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPasswordEntered.onClicked(username.getText().toString(),
                                password.getText().toString(), checkBox.isChecked());

                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPasswordEntered.onCanceled();
                    }
                }).show();
    }

    public static interface OnEditTextDialogClicked {
        void onClicked(String text);
    }

    public static interface OnPasswordEntered {
        void onClicked(String username, String password, boolean savePassword);
        void onCanceled();
    }

}
