package com.manichord.mgit.dialogs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.manichord.mgit.common.BaseViewModel
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.repo.CloneTask
import timber.log.Timber

class CloneViewModel : BaseViewModel() {

    private val _remoteUrl : MutableLiveData<String> = MutableLiveData()
    private val _localRepoName : MutableLiveData<String> = MutableLiveData()

    val remoteUrl : LiveData<String> = _remoteUrl
    val localRepoName : LiveData<String> = _localRepoName
    var cloneRecursively : Boolean = false

    var remoteUrlError : String? = null
    var localRepoNameError : String? = null

    val visible : MutableLiveData<Boolean> = MutableLiveData()

    init {
        visible.value = false
    }

    fun show(show : Boolean) {
        visible.value = show
    }

    fun setRemoteUrl(remoteUrl: String) {
        _remoteUrl.value = remoteUrl
        _localRepoName.value = stripGitExtension(stripUrlFromRepo(remoteUrl))
    }

    fun setLocalRepoName(repoName: String) {
        _localRepoName.value = repoName
    }

    fun cloneRepo() {
        // FIXME: createRepo should not use user visible strings, instead will need to be refactored
        // to set an observable state
        Timber.d("CLONE REPO %s %s", localRepoName.value, remoteUrl.value)
        val repo = Repo.createRepo(localRepoName.value, remoteUrl.value, "")
        val task = CloneTask(repo, cloneRecursively, "", null)
        task.executeTask()
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
