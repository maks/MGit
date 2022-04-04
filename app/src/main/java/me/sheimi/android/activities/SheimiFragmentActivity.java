package me.sheimi.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import me.sheimi.android.avatar.AvatarDownloader;
import me.sheimi.android.utils.BasicFunctions;
import me.sheimi.android.utils.Profile;
import me.sheimi.sgit.MGitApplication;
import me.sheimi.sgit.R;
import me.sheimi.sgit.dialogs.DummyDialogListener;
import me.sheimi.sgit.preference.PreferenceHelper;

public class SheimiFragmentActivity extends AppCompatActivity {

    private static final int MGIT_PERMISSIONS_REQUEST = 123;

    public static interface OnBackClickListener {
        public boolean onClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BasicFunctions.setActiveActivity(this);
        setTheme(Profile.getThemeResource(getApplicationContext()));
        updateLocale(Profile.useEnglishLocale(getApplicationContext()));
    }

    private void updateLocale(boolean useEnglishLocale) {
        final Locale locale;
        if (useEnglishLocale) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.getDefault();
        }
        final Resources r = getResources();
        final DisplayMetrics dm = r.getDisplayMetrics();
        final Configuration c = r.getConfiguration();
        if (c.locale == null || !c.locale.equals(locale)) {
            c.locale = locale;
            r.updateConfiguration(c, dm);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MGIT_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied
                    showMessageDialog(R.string.dialog_not_supported, getString(R.string.dialog_permission_not_granted));
                }
                return;
            }
        }
    }

    protected void checkAndRequestRequiredPermissions(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it from user
            ActivityCompat.requestPermissions(this, new String[]{permission}, MGIT_PERMISSIONS_REQUEST);
        }
    }

    public boolean checkAndRequestAccessAllFilesPermission(int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()) {
            return false;
        }
        showMessageDialog(
            R.string.dialog_access_all_files_title,
            R.string.dialog_access_all_files_msg,
            R.string.label_ok, (dialog, which) -> {
                try {
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivityForResult(intent, requestCode);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, requestCode);
                }
            }
        );
        return true;
    }

    /* View Utils Start */
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
            int negativeBtn, DialogInterface.OnClickListener positiveListener,
            DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(msg)
                .setPositiveButton(positiveBtn, positiveListener)
                .setNegativeButton(negativeBtn, negativeListener).show();
    }
    
    public void showMessageDialog(int title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(msg)
                .setPositiveButton(R.string.label_ok, new DummyDialogListener()).show();
    }

    public void showOptionsDialog(int title,final int option_names,
                                  final onOptionDialogClicked[] option_listeners) {
        CharSequence[] options_values = getResources().getStringArray(option_names);
        showOptionsDialog(title, options_values, option_listeners);
    }

    public void showOptionsDialog(int title, CharSequence[] option_values,
                                  final onOptionDialogClicked[] option_listeners) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(option_values,new DialogInterface.OnClickListener() {
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
        final EditText editText = (EditText) layout.findViewById(R.id.editText);
        editText.setHint(hint);
        builder.setTitle(title)
                .setView(layout)
                .setPositiveButton(positiveBtn,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                String text = editText.getText().toString();
                                if (text == null || text.trim().isEmpty()) {
                                    showToastMessage(R.string.alert_you_should_input_something);
                                    return;
                                }
                                positiveListener.onClicked(text);
                            }
                        })
                .setNegativeButton(R.string.label_cancel,
                        new DummyDialogListener()).show();
    }

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
        final EditText username = (EditText) layout.findViewById(R.id.username);
        final EditText password = (EditText) layout.findViewById(R.id.password);
        final CheckBox checkBox = (CheckBox) layout
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

    public static interface onOptionDialogClicked {
        void onClicked();
    }

    public static interface OnEditTextDialogClicked {
        void onClicked(String text);
    }

    /**
     * Callback interface to receive credentials entered via UI by the user after being prompted
     * in the UI in order to connect to a remote repo
     */
    public static interface OnPasswordEntered {

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
                .showImageForEmptyUri(VectorDrawableCompat.create(getResources(), R.drawable.ic_default_author, getTheme()))
                .showImageOnFail(VectorDrawableCompat.create(getResources(), R.drawable.ic_default_author, getTheme()))
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
