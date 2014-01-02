SGIT
====

an unofficial git client for android

Note
-------
* All repositories are stored in [sdcard dir]/Android/data/me.sheimi.sgit/files/repo, you could manually backup repositories if you what to delete this app.
* Here is the github repo of this project: https://github.com/sheimi/SGit
* If you have any bugs (or crashes) and want to help improve this project, please open an issue in github and describe how the bug was generated so that I can make the bugs reappear and fix them.
* This app is for android 4.x. Even though it support android 2.x, I do not have time and devices to test for it.

To Do List
---------------
* private key passphrase
* dark theme
* related commits to a file
* commit graph (low priority)

Features
------------
* add remote repo
* external repo
* initial empty repo
* clone a remote repo
* pull from origin
* delete local repo
* browse files
* browse commit messages (short)
* checkout branches and tags
* http/https/ssh are supported (without private key passphrase)
* username/password authentication is supported
* search from local repositories
* private keys management
* manually choose code's language
* git diff between commits (to be enhanced)
* import copied repositories (that is, you can copy a repository from computer and import to SGit)
* checkout remote branches
* merge branches
* push merged content
* edit file (you must have some app that can edit file)
* commit and push changed files (commit all changes)
* committer information
* prompt for password
* choose not to save password and username (will not be saved in disk but may be temporarily saved in memory)
* git status
* cancel when cloning
* add modified file to stage
* git rebase
* git cherry pick
* git checkout <file> (reset changes of a file)


<a href="https://play.google.com/store/apps/details?id=me.sheimi.sgit"><img alt="Android app on Google Play" src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" /></a>



License
-------

[GPLv3](./LICENSE)

Help & Donate
------
If you want to help improve this project you could fork SGit and send pull
request.

Or you can donate buy click the following link:
<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
<input type="hidden" name="cmd" value="_s-xclick">
<input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----
">
<input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
<img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
</form>
