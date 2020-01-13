package com.manichord.mgit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.widget.Button
import kotlinx.android.synthetic.main.dialog_exception.view.*
import me.sheimi.android.views.SheimiDialogFragment
import me.sheimi.sgit.R
import me.sheimi.sgit.dialogs.DummyDialogListener
import org.acra.ACRA

class ExceptionDialog : SheimiDialogFragment() {
    private var mThrowable: Throwable? = null
    @StringRes
    private var mErrorRes: Int = 0
    @StringRes
    var errorTitleRes: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(rawActivity)
        val inflater = rawActivity.layoutInflater
        val layout = inflater.inflate(R.layout.dialog_exception, null)
        layout.error_message.setText(mErrorRes)

        builder.setView(layout)

        // set button listener
        builder.setTitle(if (errorTitleRes != 0) errorTitleRes else R.string.dialog_error_title)
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
            dismiss()
        })
        val negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener({
            dismiss()
        })
    }

    fun setThrowable(throwable: Throwable?) {
        mThrowable = throwable
    }

    fun setErrorRes(@StringRes errorRes: Int) {
        mErrorRes = errorRes
    }
}
