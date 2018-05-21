package com.manichord.mgit.tasks.repo

import me.sheimi.sgit.R
import me.sheimi.sgit.database.models.Repo
import me.sheimi.sgit.repo.tasks.repo.RepoOpTask
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.errors.CorruptObjectException
import org.eclipse.jgit.errors.NoWorkTreeException
import org.eclipse.jgit.lib.FileMode

class UpdateIndexTask(repo: Repo, val path: String, val newMode: Int) : RepoOpTask(repo) {
    override fun doInBackground(vararg params: Void?) = updateIndex()

    private fun updateIndex(): Boolean {
        val dircache: DirCache?
        try {
            dircache = mRepo.git.repository.lockDirCache()
        } catch (e: NoWorkTreeException) {
            setException(e, R.string.error_no_worktree)
            return false
        } catch (e: CorruptObjectException) {
            setException(e, R.string.error_invalid_index)
            return false
        }

        dircache[path].fileMode = FileMode.fromBits(newMode)

        try {
        } finally {
            dircache.unlock()
        }
        return true
    }
}
