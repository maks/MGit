package com.manichord.mgit

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast

class GitAPIService : Service() {
    companion object {
        const val TAG = "mgit.GitAPIService"

        // local path of the repository starting from the root directory set in MGit (string)
        const val EXTRA_REPO_LOCAL_PATH = "local_path"

        // id of the repository
        const val EXTRA_REPO_ID = "id"

        // remote to push to, defaults to origin (string)
        const val EXTRA_REMOTE = "remote"

        // used for both push and pull (string)
        const val EXTRA_FORCE = "force"

        const val EXTRA_PUSH_ALL = "push_all"
        const val EXTRA_STAGE_ALL = "stage_all"
        const val EXTRA_AMEND = "amend"
        const val EXTRA_COMMIT_MSG = "commit_msg"
        const val EXTRA_AUTHOR_NAME = "author_name"
        const val EXTRA_AUTHOR_EMAIL = "author_email"
        const val EXTRA_FILE_PATTERN = "file_pattern"
        const val EXTRA_BRANCH = "branch"
        const val EXTRA_COMMIT = "commit"

        // TODO this probably shouldn't stay here
        const val PREF_GIT_RECEIVER_WHITELIST = "git_receiver_whitelist"

        enum class Command(val string: String) {
            Invalid("Invalid"),
            Push("Push"),
            Pull("Pull"),
            Stage("Stage"),
            Commit("Commit"),
            Checkout("Checkout"),
        }
    }

    private lateinit var mMessenger: Messenger

    override fun onBind(intent: Intent): IBinder? {
        Toast.makeText(applicationContext, "binding", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
//                    MSG_CHECK -> {
//                        Toast.makeText(applicationContext, "hello!", Toast.LENGTH_SHORT).show()
////                        Log.d(TAG, "replyto is ${msg.replyTo}")
////                        msg.replyTo.send(Message.obtain(null, MSG_CHECK, 0, 0))
//                    }
                    else -> super.handleMessage(msg)
                }
            }
        })

        return mMessenger.binder
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "got ${intent?.action.toString()}, flags $flags, id $startId")
//
//        return super.onStartCommand(intent, flags, startId)
//    }
}
