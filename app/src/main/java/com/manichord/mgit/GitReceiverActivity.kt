package com.manichord.mgit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import me.sheimi.android.activities.SheimiFragmentActivity
import me.sheimi.sgit.R
import me.sheimi.sgit.database.RepoDbManager
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.SheimiAsyncTask
import me.sheimi.sgit.repo.tasks.repo.*
import java.util.*

class GitReceiverActivity : SheimiFragmentActivity(), SheimiAsyncTask.AsyncTaskCallback,
                            SheimiAsyncTask.AsyncTaskPostCallback {
    companion object {
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

        // even if an error occurs allow the application to handle it and display it
        const val EXTRA_CUSTOM_ERROR_HANDLING = "custom_error_handling"

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

    lateinit var taskLabel: TextView
    lateinit var taskProgressLabel: TextView
    lateinit var taskRunnerLabel: TextView
    lateinit var prefs: SharedPreferences

    var tasks: Queue<Intent> = LinkedList()

    private fun runTask() {
        if (tasks.isEmpty()) {
            finish()
        }
        
        val currIntent = tasks.remove()

        if (!callingPackage.isNullOrEmpty()) {
            taskRunnerLabel.text = getString(R.string.label_task_runner, callingPackage)
        }

        val command = Command.values().firstOrNull {
            it.string.lowercase() == currIntent.data?.host?.lowercase()
        } ?: Command.Invalid

        var repo: Repo? = null
        val id = currIntent.getLongExtra(EXTRA_REPO_ID, 0)
        if (id > 0) {
            // TODO check if id is valid
            repo = Repo.getRepoById(id)
        }

        if (repo == null) {
            val localPath = currIntent.getStringExtra(EXTRA_REPO_LOCAL_PATH)
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

        taskLabel.text = getString(R.string.label_task, command.string, repo.diaplayName)

        fun task() {
            when (command) {
                Command.Pull, Command.Push -> {
                    var remote = currIntent.getStringExtra(EXTRA_REMOTE)
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
                                currIntent.getBooleanExtra(EXTRA_PUSH_ALL, false),
                                currIntent.getBooleanExtra(EXTRA_FORCE, false),
                                this
                            ).executeTask()
                        }
                        Command.Pull -> {
                            PullTask(repo,
                                remote,
                                currIntent.getBooleanExtra(EXTRA_FORCE, false),
                                this
                            ).executeTask()
                        }
                        else -> {
                            throw RuntimeException("unreachable code")
                        }
                    }
                }

                Command.Commit -> {
                    val msg = currIntent.getStringExtra(EXTRA_COMMIT_MSG)
                    if (msg.isNullOrEmpty()) {
                        // TODO proper error
                        throw Exception("No commit message provided")
                    }

                    CommitChangesTask(repo,
                        msg,
                        currIntent.getBooleanExtra(EXTRA_AMEND, false),
                        currIntent.getBooleanExtra(EXTRA_STAGE_ALL, false),
                        currIntent.getStringExtra(EXTRA_AUTHOR_NAME),
                        currIntent.getStringExtra(EXTRA_AUTHOR_EMAIL),
                        this
                    ).executeTask()
                }

                Command.Stage -> {
                    val filePattern = currIntent.getStringExtra(EXTRA_FILE_PATTERN)
                    if (filePattern.isNullOrEmpty()) {
                        // TODO proper error
                        throw Exception("Invalid file pattern")
                    }

                    AddToStageTask(repo, filePattern).executeTask()
                }

                Command.Checkout -> {
                    val commit = currIntent.getStringExtra(EXTRA_COMMIT)
                    val branch = currIntent.getStringExtra(EXTRA_BRANCH)

                    if (commit.isNullOrBlank() && branch.isNullOrBlank()) {
                        // TODO proper error
                        throw Exception("Neither commit nor branch was provided for checkout command")
                    }

                    CheckoutTask(repo,
                        commit,
                        branch,
                        this
                    ).executeTask()
                }

                else -> {
                    // TODO proper error
                    throw Exception("Invalid command")
                }
            }

            this.setResult(RESULT_OK, Intent()
                .putExtra("success", true))
        }

        // this does not work for some reason TODO
        this.showOptionsDialog(R.string.dialog_confirm_task_execution_title, arrayOf("Yes", "No"), arrayOf(
            // yes
            onOptionDialogClicked {
                task()
            },

            // no
            onOptionDialogClicked {
                  if (tasks.isEmpty()) {
                      finish()
                  } else {
                      runTask()
                  }
            },

            // yes, don't bother me
            onOptionDialogClicked {
                // TODO: add package to whitelist if it exists
//                if (!callingPackage.isNullOrEmpty()) {
//                    val whitelist = HashSet(prefs.getStringSet(PREF_GIT_RECEIVER_WHITELIST, HashSet()))
//
//                    prefs.edit().putStringSet(PREF_GIT_RECEIVER_WHITELIST,).apply()
//                }
                task()
            }
        ))
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        return true
    }

    override fun onPreExecute() {}

    override fun onProgressUpdate(vararg progress: String?) {
        taskProgressLabel.text = getString(R.string.label_task_progress, progress[0], progress[2], progress[1])
    }

    override fun onPostExecute(isSuccess: Boolean?) {
        if (isSuccess == true) {
            runTask()
        } else {
            // TODO error and listen to EXTRA_CUSTOM_ERROR_HANDLING
            taskProgressLabel.text = "FAILED"
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            tasks.add(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.git_receiver_activity)

        val intent2 = Intent()
            .putStringArrayListExtra("commands", arrayListOf("pull", "push"))
            .putExtra("pull", Bundle().apply {
                putString("ahaha", "hh")
            })

        taskLabel = findViewById<TextView>(R.id.taskLabel)
        taskProgressLabel = findViewById<TextView>(R.id.taskProgressLabel)
        taskRunnerLabel = findViewById<TextView>(R.id.taskRunnerLabel)

        prefs = this.getPreferences(Context.MODE_PRIVATE)

        tasks.add(intent)
        runTask()
    }
}
