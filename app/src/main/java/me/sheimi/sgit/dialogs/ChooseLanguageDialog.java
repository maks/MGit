package me.sheimi.sgit.dialogs;

import java.util.List;

import me.sheimi.android.utils.CodeGuesser;
import me.sheimi.android.views.SheimiDialogFragment;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.ViewFileActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by sheimi on 8/16/13.
 */
public class ChooseLanguageDialog extends SheimiDialogFragment {

    private ViewFileActivity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (ViewFileActivity) getActivity();

        final List<String> langs = CodeGuesser.getLanguageList();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setTitle(R.string.dialog_choose_language_title);
        builder.setItems(langs.toArray(new String[0]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                            int position) {
                        String lang = langs.get(position);
                        String tag = CodeGuesser.getLanguageTag(lang);
                        mActivity.setLanguage(tag);
                    }
                });

        return builder.create();
    }

}
