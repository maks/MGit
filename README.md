SGIT
====

An unofficial Git client for Android

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
<a href="https://f-droid.org/repository/browse/?fdfilter=sgit&fdid=me.sheimi.sgit"><img alt="Android app on F-Droid" src="https://fsfe.org/campaigns/android/f-droid.png" width="45" /></a>

Quick start
-----------
### To clone a remote repository:
1. Click on the *+* icon to add a new repository.
2. Enter remote URL (see URL format below)
3. Enter local repo name - note that this is not a path since sGit stores all repos in the same directory on the mobile device.
4. Username - username to use for remote repo.
5. Password - password for remote repo.
6. Click the *Clone* button.
7. If all the credentials are correct, sGit will download the repo (all branches) to your device.

### To create a local repository:
1. Click on the *+* icon to add a new repository.
2. Click on "Init Local" to create a local repository.
3. Enter the name for this repository when prompted.
4. A local repo will be created.

### URL format
#### SSH URLs
* SSH running on standard port (22): `ssh://username@server_name/path/to/repo`
* SSH running on non-standard port: `ssh://username@server_name:port/path/to/repo`
* `username` is needed - by default, sGit tries to connect as root!

#### HTTP(S) URLs
* HTTPS URL: https://server_name/path/to/repo

License
-------

[GPLv3](./LICENSE)

Help & Donate
------
If you want to help improve this project you could fork SGit and send pull request.

If you want to donate this project, you can donate via paypal

<a target='_blank' href="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=KWFGX7RNJ6LM8&lc=US&item_name=Donate%20SGit&item_number=sgit&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"><img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif"></a>

Or donate via [支付宝](https://me.alipay.com/sheimi)
