package com.manichord.mgit.ssh

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import me.sheimi.android.views.SheimiDialogFragment
import me.sheimi.sgit.R
import me.sheimi.sgit.activities.explorer.PrivateKeyManageActivity
import me.sheimi.sgit.ssh.PrivateKeyUtils
import java.io.File
import java.io.FileOutputStream

class PrivateKeyGenerate : SheimiDialogFragment() {
    private lateinit var mNewFilename: EditText
    private lateinit var mKeyLength: EditText
    private lateinit var mRadioGroup: RadioGroup

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_generate_key, null)
        mNewFilename = view.findViewById(R.id.newFilename)
        mKeyLength = view.findViewById(R.id.key_size)
        mKeyLength.setText("4096")
        mRadioGroup = view.findViewById(R.id.radio_keygen_type)
        builder.setMessage(R.string.label_dialog_generate_key)
                .setView(view)
                .setPositiveButton(R.string.label_generate_key) { _, _ -> generateKey() }
                .setNegativeButton(R.string.label_cancel) { _, _ -> }
        return builder.create()
    }

    private fun generateKey() {
        val newFilename = mNewFilename.text.toString().trim { it <= ' ' }
        if (newFilename == "") {
            showToastMessage(R.string.alert_new_filename_required)
            mNewFilename.error = getString(R.string.alert_new_filename_required)
            return
        }
        if (newFilename.contains("/")) {
            showToastMessage(R.string.alert_filename_format)
            mNewFilename.error = getString(R.string.alert_filename_format)
            return
        }
        val keySize = mKeyLength.text.toString().toInt()
        if (keySize < 1024) {
            showToastMessage(R.string.alert_too_short_key_size)
            mNewFilename.error = getString(R.string.alert_too_short_key_size)
            return
        }
        if (keySize > 16384) {
            showToastMessage(R.string.alert_too_long_key_size)
            mNewFilename.error = getString(R.string.alert_too_long_key_size)
            return
        }
        val type = when (mRadioGroup.checkedRadioButtonId) {
            R.id.radio_dsa -> KeyPair.DSA
            // JSCH doesn't support writing ED25519 keys yet, only reading
            //R.id.radio_ed25519 -> KeyPair.ED25519
            else -> KeyPair.RSA
        }
        val newKey = File(PrivateKeyUtils.getPrivateKeyFolder(), newFilename)
        val newPubKey = File(PrivateKeyUtils.getPublicKeyFolder(), newFilename)
        try {
            val jsch = JSch()
            val kpair = KeyPair.genKeyPair(jsch, type, keySize)
            kpair.writePrivateKey(FileOutputStream(newKey))
            kpair.writePublicKey(FileOutputStream(newPubKey), "mgit")
            kpair.dispose()
        } catch (e: Exception) {
            //TODO
            e.printStackTrace()
        }
        (activity as PrivateKeyManageActivity?)!!.refreshList()
    }
}
