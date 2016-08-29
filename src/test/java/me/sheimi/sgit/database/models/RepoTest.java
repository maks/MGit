package me.sheimi.sgit.database.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class RepoTest {

    @Test
    public void testGetCurrentDisplayName() throws Exception {
        assertEquals("", "remotes/foo", Repo.getCommitDisplayName("refs/remotes/foo"));
        assertEquals("", "remotes/foo/bar", Repo.getCommitDisplayName("refs/remotes/foo/bar"));
        assertEquals("", "foo", Repo.getCommitDisplayName("refs/heads/foo"));
        assertEquals("", "foo/bar", Repo.getCommitDisplayName("refs/heads/foo/bar"));
        assertEquals("", "foo/bar", Repo.getCommitDisplayName("refs/tags/foo/bar"));
        assertEquals("", "foo/bar", Repo.getCommitDisplayName("refs/tags/foo/bar"));
        assertEquals("", "refs/blah/foo/bar", Repo.getCommitDisplayName("refs/blah/foo/bar"));

        assertEquals("incomlete ref name must return itself string, not cause exception", "re", Repo.getCommitDisplayName("re"));
    }

    @Test
    public void testgetCommitType() {
        assertEquals("should be type head", Repo.COMMIT_TYPE_HEAD, Repo.getCommitType("refs/heads/foo"));
        assertEquals("should be type head", Repo.COMMIT_TYPE_HEAD, Repo.getCommitType("refs/heads/foo/bar"));
        assertEquals("should be type remote", Repo.COMMIT_TYPE_REMOTE, Repo.getCommitType("refs/remotes/foo"));
        assertEquals("should be type remote", Repo.COMMIT_TYPE_REMOTE, Repo.getCommitType("refs/remotes/foo/bar"));
        assertEquals("should be type remote", Repo.COMMIT_TYPE_TAG, Repo.getCommitType("refs/tags/foo/bar"));
        assertEquals("should be type remote", Repo.COMMIT_TYPE_TAG, Repo.getCommitType("refs/tags/foo/bar"));
        assertEquals("should be type remote", Repo.COMMIT_TYPE_UNKNOWN, Repo.getCommitType("refs/blah/foo/bar"));
    }
}