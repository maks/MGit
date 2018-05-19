package com.manichord.mgit.dialogs

import android.arch.lifecycle.MutableLiveData
import com.manichord.mgit.common.BaseViewModel
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.repo.CloneTask
import timber.log.Timber

class CloneViewModel : BaseViewModel() {

    var remoteUrl : String = ""
        set(value) {
            field = value
            localRepoName.value = stripGitExtension(stripUrlFromRepo(remoteUrl))
        }

    val localRepoName : MutableLiveData<String> = MutableLiveData()
    var cloneRecursively : Boolean = false
    val initLocal : MutableLiveData<Boolean> = MutableLiveData()

    var remoteUrlError : String? = null
    var localRepoNameError : String? = null

    val visible : MutableLiveData<Boolean> = MutableLiveData()

    init {
        visible.value = false
        initLocal.value = false
    }

    fun show(show : Boolean) {
        visible.value = show
    }

    fun setLocalRepoName(repoName: String) {
        localRepoName.value = repoName
    }

    fun cloneRepo() {
        // FIXME: createRepo should not use user visible strings, instead will need to be refactored
        // to set an observable state
        if (initLocal?.value == true) {
            Timber.d("INIT LOCAL %s", localRepoName.value)
            //TODO:
        } else {
            Timber.d("CLONE REPO %s %s [%b]", localRepoName.value, remoteUrl, cloneRecursively)
            val repo = Repo.createRepo(localRepoName.value, remoteUrl, "")
            val task = CloneTask(repo, cloneRecursively, "", null)
            task.executeTask()
        }
    }

    private fun stripUrlFromRepo(remoteUrl: String): String {
        val lastSlash = remoteUrl.lastIndexOf("/")
        return if (lastSlash != -1) {
            remoteUrl.substring(lastSlash + 1)
        } else remoteUrl

    }

    private fun stripGitExtension(remoteUrl: String): String {
        val extension = remoteUrl.indexOf(".git")
        return if (extension != -1) {
            remoteUrl.substring(0, extension)
        } else remoteUrl

    }
}
