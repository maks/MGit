package me.sheimi.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

import me.sheimi.android.avatar.AvatarDownloader;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.utils.Profile;
import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.DummyDialogListener;

public abstract class SheimiFragmentActivity extends AppCompatActivity {

    public interface OnBackClickListener {
        boolean onClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BasicFunctions.setActiveActivity(this);
        setTheme(Profile.getThemeResource(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        BasicFunctions.setActiveActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mImageLoader != null && mImageLoader.isInited()) {
            mImageLoader.destroy();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }

    /* View Utils Start */
    // TODO: Move them to Kotlin extension methods for Context

    public void showToastMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SheimiFragmentActivity.this, msg,
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showToastMessage(int resId) {
        showToastMessage(getString(resId));
    }

    public void showMessageDialog(int title, int msg, int positiveBtn,
                                  DialogInterface.OnClickListener positiveListener) {
        showMessageDialog(title, getString(msg), positiveBtn,
            R.string.label_cancel, positiveListener,
            new DummyDialogListener());
    }

    public void showMessageDialog(int title, String msg, int positiveBtn,
                                  DialogInterface.OnClickListener positiveListener) {
        showMessageDialog(title, msg, positiveBtn, R.string.label_cancel,
            positiveListener, new DummyDialogListener());
    }

    public void showMessageDialog(int title, String msg, int positiveBtn,
                                  @SuppressWarnings("SameParameterValue") int negativeBtn, DialogInterface.OnClickListener positiveListener,
                                  DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(msg)
            .setPositiveButton(positiveBtn, positiveListener)
            .setNegativeButton(negativeBtn, negativeListener).show();
    }

    public void showMessageDialog(int title, String msg) {
        showMessageDialog(this, title, msg);
    }

    // TODO: Replace with Kotlin's extension methods
    public static void showMessageDialog(Context ctx, int title, String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title).setMessage(msg)
            .setPositiveButton(R.string.label_ok, new DummyDialogListener()).show();
    }

    public void showOptionsDialog(int title, final int option_names,
                                  final onOptionDialogClicked[] option_listeners) {
        CharSequence[] options_values = getResources().getStringArray(option_names);
        showOptionsDialog(title, options_values, option_listeners);
    }

    public void showOptionsDialog(int title, CharSequence[] option_values,
                                  final onOptionDialogClicked[] option_listeners) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(option_values, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                option_listeners[which].onClicked();
            }
        }).create().show();
    }

    public void showEditTextDialog(int title, int hint, int positiveBtn,
                                   final OnEditTextDialogClicked positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText editText = layout.findViewById(R.id.editText);
        editText.setHint(hint);
        builder.setTitle(title)
            .setView(layout)
            .setPositiveButton(positiveBtn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                        DialogInterface dialogInterface, int i) {
                        String text = editText.getText().toString();
                        if (text.trim().isEmpty()) {
                            showToastMessage(R.string.alert_you_should_input_something);
                            return;
                        }
                        positiveListener.onClicked(text);
                    }
                })
            .setNegativeButton(R.string.label_cancel,
                new DummyDialogListener()).show();
    }

    // Why this infinite recursion?
    public void promptForPassword(OnPasswordEntered onPasswordEntered,
                                  int errorId) {
        promptForPassword(onPasswordEntered, errorId);
    }

    public void promptForPassword(final OnPasswordEntered onPasswordEntered,
                                  final String errorInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                promptForPasswordInner(onPasswordEntered, errorInfo);
            }
        });
    }

    private void promptForPasswordInner(
        final OnPasswordEntered onPasswordEntered, String errorInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_prompt_for_password,
            null);
        final EditText username = layout.findViewById(R.id.username);
        final EditText password = layout.findViewById(R.id.password);
        final CheckBox checkBox = layout
            .findViewById(R.id.savePassword);
        if (errorInfo == null) {
            errorInfo = getString(R.string.dialog_prompt_for_password_title);
        }
        builder.setTitle(errorInfo)
            .setView(layout)
            .setPositiveButton(R.string.label_done,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPasswordEntered.onClicked(username.getText()
                            .toString(), password.getText()
                            .toString(), checkBox.isChecked());

                    }
                })
            .setNegativeButton(R.string.label_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                        DialogInterface dialogInterface, int i) {
                        onPasswordEntered.onCanceled();
                    }
                }).show();
    }

    public interface onOptionDialogClicked {
        void onClicked();
    }

    public interface OnEditTextDialogClicked {
        void onClicked(String text);
    }

    /**
     * Callback interface to receive credentials entered via UI by the user after being prompted
     * in the UI in order to connect to a remote repo
     */
    public interface OnPasswordEntered {

        /**
         * Handle retrying a Remote Repo task after user supplies requested credentials
         *
         * @param username
         * @param password
         * @param savePassword
         */
        void onClicked(String username, String password, boolean savePassword);

        void onCanceled();
    }

    /* View Utils End */

    /* Switch Activity Animation Start */
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        forwardTransition();
    }

    public void finish() {
        super.finish();
        backTransition();
    }

    public void rawfinish() {
        super.finish();
    }

    public void forwardTransition() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void backTransition() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /* Switch Activity Animation End */

    /* ImageCache Start */

    private static final int SIZE = 100 << 20;
    private ImageLoader mImageLoader;

    private void setupImageLoader() {
        DisplayImageOptions mDisplayOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageForEmptyUri(R.drawable.ic_default_author)
            .showImageOnFail(R.drawable.ic_default_author)
            .build();
        File cacheDir = StorageUtils.getCacheDirectory(this);
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(mDisplayOptions)
            .diskCache(new UnlimitedDiskCache(cacheDir))
            .diskCacheSize(SIZE)
            .imageDownloader(new AvatarDownloader(this))
            .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(configuration);
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null || !mImageLoader.isInited()) {
            setupImageLoader();
        }
        return mImageLoader;
    }
    /* ImageCache End */
}
