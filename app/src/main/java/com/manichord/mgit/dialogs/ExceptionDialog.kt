package com.manichord.mgit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.widget.Button
import me.sheimi.android.views.SheimiDialogFragment
import me.sheimi.sgit.R
import com.manichord.mgit.repolist.RepoListActivity
import me.sheimi.sgit.dialogs.DummyDialogListener
import org.acra.ACRA

class ExceptionDialog : SheimiDialogFragment() {

    private lateinit var mActivity: RepoListActivity
    private lateinit var mThrowable: Throwable
    @StringRes
    private var mErrorRes: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        mActivity = activity as RepoListActivity

        val builder = AlertDialog.Builder(mActivity)
        val inflater = mActivity.layoutInflater
        val layout = inflater.inflate(R.layout.dialog_exception, null)

        builder.setView(layout)

        // set button listener
        builder.setTitle(if (mErrorRes != 0) mErrorRes else R.string.dialog_error_title)
        builder.setNegativeButton(getString(R.string.label_cancel),
                DummyDialogListener())
        builder.setPositiveButton(
                getString(R.string.dialog_error_send_report),
                DummyDialogListener())

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE) as Button
        positiveButton.setOnClickListener({
            ACRA.getErrorReporter().handleException(mThrowable, false)
        })
        val negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener({
            dismiss()
        })
    }

    fun setThrowable(throwable: Throwable) {
        mThrowable = throwable
    }

    fun setErrorRes(@StringRes errorRes: Int) {
        mErrorRes = errorRes
    }
}
