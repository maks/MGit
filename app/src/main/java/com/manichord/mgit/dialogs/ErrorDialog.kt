package com.manichord.mgit.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.widget.Button
import io.sentry.Sentry
import kotlinx.android.synthetic.main.dialog_error.view.*
import me.sheimi.android.views.SheimiDialogFragment
import me.sheimi.sgit.BuildConfig
import me.sheimi.sgit.R
import me.sheimi.sgit.dialogs.DummyDialogListener
import timber.log.Timber

class ErrorDialog : SheimiDialogFragment() {
    private var mThrowable: Throwable? = null
    @StringRes
    private var mErrorRes: Int = 0
    @StringRes
    var errorTitleRes: Int = 0
        get() = if (field != 0) field else R.string.dialog_error_title

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(rawActivity)
        val inflater = rawActivity.layoutInflater
        val layout = inflater.inflate(R.layout.dialog_error, null)
        val details = when (mThrowable) {
            is Exception -> {
                (mThrowable as Exception).message
            }
            else -> ""
        }
        layout.error_message.setText(getString(mErrorRes) + "\n" + details)

        builder.setView(layout)

        // set button listener
        builder.setTitle(errorTitleRes)
        builder.setPositiveButton(
            getString(R.string.label_ok),
            DummyDialogListener())
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog
        val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE) as Button
        positiveButton.setOnClickListener {
            if (BuildConfig.DEBUG) {
                // when debugging just log the exception
                if (mThrowable != null) {
                    Timber.e(mThrowable);
                } else {
                    Timber.e(if (mErrorRes != 0) getString(mErrorRes) else "")
                }
            } else {
                mThrowable?.let { Sentry.capture(mThrowable) }
            }
            dismiss()
        }
    }

    fun setThrowable(throwable: Throwable?) {
        mThrowable = throwable
    }

    fun setErrorRes(@StringRes errorRes: Int) {
        mErrorRes = errorRes
    }
}
