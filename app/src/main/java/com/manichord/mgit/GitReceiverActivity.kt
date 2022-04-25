package com.manichord.mgit

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.sheimi.android.activities.SheimiFragmentActivity
import me.sheimi.sgit.R
import me.sheimi.sgit.database.RepoDbManager
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask
import me.sheimi.sgit.repo.tasks.repo.*

class GitReceiverActivity : SheimiFragmentActivity() {
    companion object {
        // local path of the repository starting from the root directory set in MGit (string)
        const val EXTRA_REPO_LOCAL_PATH = "local_path"

        // id of the repository, starting from 1 (long)
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

        enum class Command(val string: String) {
            Invalid("Invalid"),
            Push("Push"),
            Pull("Pull"),
            Stage("Stage"),
            Commit("Commit"),
            Checkout("Checkout"),
        }
    }

    private val notificationChannel = "com.manichord.mgit.GitReceiverActivity"
    private val notificationId = 0 // what to put here?
    private lateinit var notification: NotificationCompat.Builder

    class Callback(val context: Context, val activity: GitReceiverActivity) : SheimiAsyncTask.AsyncTaskCallback,
        SheimiAsyncTask.AsyncTaskPostCallback {

        override fun doInBackground(vararg params: Void?): Boolean {
            return true
        }

        override fun onPreExecute() {}

        override fun onProgressUpdate(vararg progress: String?) {
            this.activity.notification
                .setContentText("${progress[0]} ${progress[2]}")
                .setProgress(100, progress[3]!!.toInt(), false)

            with(NotificationManagerCompat.from(context)) {
                notify(activity.notificationId, activity.notification.build())
            }
        }

        override fun onPostExecute(isSuccess: Boolean?) {
            if (isSuccess == true) {
                with(NotificationManagerCompat.from(context)) {
                    cancel(activity.notificationId)
                }
            } else {
                // keep a notification if the task failed
                this.activity.notification.setContentText("Task has failed")
                    .setProgress(0, 0, false)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("TODO placeholder for information about the failure")
                    )
                    .setOngoing(false)

                with(NotificationManagerCompat.from(context)) {
                    notify(activity.notificationId, activity.notification.build())
                }
            }

            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.notification = NotificationCompat.Builder(applicationContext, this.notificationChannel)
            .setSmallIcon(R.drawable.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)

        val command = Command.values().firstOrNull {
            it.string.lowercase() == intent.data?.host?.lowercase()
        } ?: Command.Invalid

        var repo: Repo? = null
        val id = intent.getLongExtra(EXTRA_REPO_ID, 0)
        if (id > 0) {
            // TODO check if id is valid
            repo = Repo.getRepoById(id)
        }

        if (repo == null) {
            val localPath = intent.getStringExtra(EXTRA_REPO_LOCAL_PATH)
            if (localPath.isNullOrEmpty()) {
                // TODO proper error
                throw Exception("Invalid repository id and/or local path")
            }

            val cursor = RepoDbManager.queryAllRepo()
            val repos = Repo.getRepoList(applicationContext, cursor)
            repo = repos.firstOrNull { it.localPath == localPath }
                ?:
                // TODO proper error
                throw Exception("Invalid repository id and/or local path")
        }

        // set the notification title to the command name
        this.notification.setContentTitle("${command.string} ${repo.localPath}")

        when (command) {
            Command.Pull, Command.Push -> {
                var remote = intent.getStringExtra(EXTRA_REMOTE)
                if (remote.isNullOrEmpty()) {
                    // default to first remote
                    remote = repo.remotes.firstOrNull()
                        ?:
                        // TODO proper error
                        throw Exception("Repository contains no remotes")
                }

                if (!repo.remotes.contains(remote)) {
                    // TODO proper error
                    throw Exception("Invalid remote")
                }

                when (command) {
                    Command.Push -> {
                        PushTask(repo,
                            remote,
                            intent.getBooleanExtra(EXTRA_PUSH_ALL, false),
                            intent.getBooleanExtra(EXTRA_FORCE, false),
                            Callback(applicationContext, this)
                        ).executeTask()
                    }
                    Command.Pull -> {
                        PullTask(repo,
                            remote,
                            intent.getBooleanExtra(EXTRA_FORCE, false),
                            Callback(applicationContext, this)
                        ).executeTask()
                    }
                    else -> {
                        throw RuntimeException("unreachable code")
                    }
                }
            }

            Command.Commit -> {
                val msg = intent.getStringExtra(EXTRA_COMMIT_MSG)
                if (msg.isNullOrEmpty()) {
                    // TODO proper error
                    throw Exception("No commit message provided")
                }

                this.notification.setContentText("Commiting changes")
//                    .setOngoing(true)

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationId, notification.build())
                }

                CommitChangesTask(repo,
                    msg,
                    intent.getBooleanExtra(EXTRA_AMEND, false),
                    intent.getBooleanExtra(EXTRA_STAGE_ALL, false),
                    intent.getStringExtra(EXTRA_AUTHOR_NAME),
                    intent.getStringExtra(EXTRA_AUTHOR_EMAIL),
                    Callback(applicationContext, this)
                ).executeTask()
            }

            Command.Stage -> {
                val filePattern = intent.getStringExtra(EXTRA_FILE_PATTERN)
                if (filePattern.isNullOrEmpty()) {
                    // TODO proper error
                    throw Exception("Invalid file pattern")
                }

                AddToStageTask(repo, filePattern).executeTask()
            }

            Command.Checkout -> {
                val commit = intent.getStringExtra(EXTRA_COMMIT)
                val branch = intent.getStringExtra(EXTRA_BRANCH)

                if (commit.isNullOrBlank() && branch.isNullOrBlank()) {
                    // TODO proper error
                    throw Exception("Neither commit nor branch was provided for checkout command")
                }

                this.notification.setContentText("???") // TODO
                    .setOngoing(true)

                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(notificationId, notification.build())
                }

                CheckoutTask(repo,
                    commit,
                    branch,
                    Callback(applicationContext, this)
                ).executeTask()
            }

            else -> {
                // TODO proper error
                throw Exception("Invalid command")
            }
        }
    }
}
