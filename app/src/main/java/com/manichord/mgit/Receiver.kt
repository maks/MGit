package com.manichord.mgit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.sheimi.sgit.R
import me.sheimi.sgit.database.RepoDbManager
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask
import me.sheimi.sgit.repo.tasks.repo.PullTask
import me.sheimi.sgit.repo.tasks.repo.PushTask

class Receiver : BroadcastReceiver() {
    private val notification_channel = "com.manichord.mgit.receiver"
    private val notification_id = 4 // what to put here?
    private lateinit var notification: NotificationCompat.Builder

    // TODO Make the receiver itself also a callback so there is no need to pass things around?
    class Callback(val context: Context, val receiver: Receiver) : SheimiAsyncTask.AsyncTaskCallback {
        override fun doInBackground(vararg params: Void?): Boolean {
            return true
        }

        override fun onPreExecute() {}

        override fun onProgressUpdate(vararg progress: String?) {
            this.receiver.notification
                .setContentText("${progress[0]} ${progress[2]}")
                .setProgress(100, progress[3]!!.toInt(), false)

            with(NotificationManagerCompat.from(context)) {
                notify(receiver.notification_id, receiver.notification.build())
            }
        }

        override fun onPostExecute(isSuccess: Boolean?) {
            with(NotificationManagerCompat.from(context)) {
                cancel(receiver.notification_id)
            }

            // TODO error if it failed?

            // send an intent back for checking
            val intent = Intent("com.manichord.mgit.GIT")
                .putExtra("success", isSuccess == true)

            this.context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        this.notification = NotificationCompat.Builder(context, this.notification_channel)
            .setSmallIcon(R.drawable.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        // TODO make these into functions so sync could just call them and not duplicate code
        when (intent.action) {
            "com.manichord.mgit.PULL" -> {
                this.notification.setContentText("Pull")

                var repo: Repo? = null
                val id = intent.getLongExtra("id", 0)
                if (id > 0) {
                    repo = Repo.getRepoById(id)
                }

                if (repo == null) {
                    val localPath = intent.getStringExtra("local_path")
                    if (localPath.isNullOrEmpty()) {
                        // TODO proper error
                        throw Exception("Invalid arguments passed")
                    }

                    val cursor = RepoDbManager.queryAllRepo()
                    val repos = Repo.getRepoList(context, cursor)

                    for (i in repos) {
                        if (i.localPath == localPath) {
                            repo = i
                            break
                        }
                    }
                }

                if (repo == null) {
                    // TODO proper error
                    throw Exception("Invalid arguments passed")
                }

                var remote = intent.getStringExtra("remote")
                if (remote.isNullOrEmpty()) {
                    remote = "origin"
                }

                if (!repo.remotes.contains(remote)) {
                    // TODO proper error
                    throw Exception("Invalid remote passed")
                }

                PullTask(repo,
                    remote,
                    intent.getBooleanExtra("force_pull", false),
                    Callback(context, this)).executeTask()
            }

            "com.manichord.mgit.PUSH" -> {
                this.notification.setContentText("Push")

                var repo: Repo? = null
                val id = intent.getLongExtra("id", 0)
                if (id > 0) {
                    repo = Repo.getRepoById(id)
                }

                if (repo == null) {
                    val localPath = intent.getStringExtra("local_path")
                    if (localPath.isNullOrEmpty()) {
                        // TODO proper error
                        throw Exception("Invalid arguments passed")
                    }

                    val cursor = RepoDbManager.queryAllRepo()
                    val repos = Repo.getRepoList(context, cursor)

                    for (i in repos) {
                        if (i.localPath == localPath) {
                            repo = i
                            break
                        }
                    }
                }

                if (repo == null) {
                    // TODO proper error
                    throw Exception("Invalid arguments passed")
                }

                var remote = intent.getStringExtra("remote")
                if (remote.isNullOrEmpty()) {
                    remote = "origin"
                }

                if (!repo.remotes.contains(remote)) {
                    // TODO proper error
                    throw Exception("Invalid remote passed")
                }

                PushTask(repo,
                    remote,
                    intent.getBooleanExtra("push_all", false),
                    intent.getBooleanExtra("force_push", false),
                    Callback(context, this)).executeTask()
            }
        }
    }
}
