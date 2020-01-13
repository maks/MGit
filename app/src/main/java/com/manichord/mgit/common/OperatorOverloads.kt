@file:Suppress("NOTHING_TO_INLINE")
package com.manichord.mgit.common

import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.dircache.DirCacheEntry

internal inline operator fun DirCache.get(path: String): DirCacheEntry? = getEntry(path)
internal inline operator fun DirCache.get(i: Int): DirCacheEntry = getEntry(i)
