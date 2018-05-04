package com.manichord.mgit.dialogs

import android.databinding.Bindable
import com.manichord.mgit.common.BaseViewModel
import me.sheimi.sgit.BR
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.repo.CloneTask
import timber.log.Timber

class CloneDialogViewModel(url: String) : BaseViewModel() {

    var remoteUrl : String = ""
        @Bindable
        set(value) {
            field = value
            notifyPropertyChanged(BR.localRepoName)
        }

    var localRepoName : String = ""
        @Bindable
        get() {
            if (remoteUrl.isEmpty()) { return "" }
            return stripGitExtension(stripUrlFromRepo(remoteUrl))
        }

    var cloneRecursively : Boolean = false

    var remoteUrlError : String? = null
    var localRepoNameError : String? = null

    init {
        remoteUrl = url
    }

    fun cloneRepo() {
        // FIXME: createRepo should not use user visible strings, instead will need to be refactored
        // to set an observable state
        Timber.d("CLONE REPO %s %s", localRepoName, remoteUrl)
        val repo = Repo.createRepo(localRepoName, remoteUrl, "")
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
