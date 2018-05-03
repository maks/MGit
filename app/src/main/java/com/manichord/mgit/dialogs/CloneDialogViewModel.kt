package com.manichord.mgit.dialogs

import android.databinding.Bindable
import com.manichord.mgit.common.BaseViewModel
import me.sheimi.sgit.BR

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

    init {
        remoteUrl = url
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
