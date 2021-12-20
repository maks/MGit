# MGit

MGit is a Git client Android App.

This is a continuation of [the SGit project](https://github.com/sheimi/SGit).

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=com.manichord.mgit)
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/com.manichord.mgit)

## Notes

[![Build Status](https://travis-ci.org/maks/MGit.svg?branch=master)](https://travis-ci.org/maks/MGit)

[![Join the chat at https://gitter.im/MGit-Android/Lobby](https://badges.gitter.im/MGit-Android/Lobby.svg)](https://gitter.im/MGit-Android/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Translate - with Stringlate](https://img.shields.io/badge/translate%20with-stringlate-green.svg)](https://lonamiwebs.github.io/stringlate/translate?git=https%3A%2F%2Fgithub.com%2Fmaks%2FMGit)

* If you encounter any issues (bugs, crashes, etc.) and want to help improve this project, please open an issue on [GitHub](https://github.com/maks/MGit/issues/new) describing: what the issues are; and how they were caused, to allow for re-creation and fixing of bugs.
* This app requires minimum of for Android v5.0

### Editing Files

As of version 1.5.7, MGit no longer provides an internal texteditor, instead if you wish to edit files, you will need to have an editor app installed. 

An open source editor that has been tested to work with MGit is ["Turbo Editor"]( https://play.google.com/store/apps/details?id=com.maskyn.fileeditorpro)

but others that support File Providers should also work.

## Supported Features

* Create local repositories
* Clone remote repositories
* Pull from origin
* Delete local repositories
* Browse files
* Browse commit messages (short)
* Checkout branches and tags
* HTTP/HTTPS/SSH are supported (including SSH with private key passphrase)
* Username/Password authentication is supported
* Search local repositories
* Private key management
* Manually choose code language
* `git diff` between commits
* Import existing repositories (that is, you can copy a repository from computer and import to MGit)
* Checkout remote branches
* Merge branches
* Push merged content
* Edit file via external app that can edit the given file type
* Commit and push changed files
* Committer information
* Prompt for password
* *Option* to save username/password
* `git status`
* Cancel when cloning
* Add modified file to stage
* View state of staged files (aka index)
* `git rebase`
* `git cherrypick`
* `git checkout <file>` (reset changes of a file)

## Quick start

### Clone a remote repository

1. Click on the `+` icon to add a new repository
2. Enter remote URL (see URL format below)
3. Enter local repository name - note that this is **not** the full path, as MGit stores all  
repositories in the same local directory (can be changed in MGit settings)
4. Click the `Clone` button
5. If required, you will be prompted for credentials to connect to the remote repo. MGit will download the repository (all branches) to your device

### Create a local repository
1. Click on the `+` icon to add a new repository
2. Click on `Init Local` to create a local repository
3. Enter the name for this repository when prompted
4. A local empty repo will be created

### URL format

#### SSH URLs

* SSH running on standard port (22): `ssh://username@server_name/path/to/repo`
* SSH running on non-standard port: `ssh://username@server_name:port/path/to/repo`
* `username` is needed - by default, MGit tries to connect as root.

#### HTTP(S) URLs

* HTTP(S) URL: `https://server_name/path/to/repo`

## ToDo List

[Future enhancements and bugs are tracked here on Github](https://github.com/maks/MGit/issues).

## License

See [GPLv3](./LICENSE)

All code written by `maks@manichord.com` can at your option also be used under the [MIT license](https://en.wikipedia.org/wiki/MIT_License).

## Help

If you want to help improve this project, contributions, especially translations are very welcome. Also contributions to documentation via the wiki for this repo are also most welcome!

### Contributing code

If you would like to contribute code, either a bugfix or a new feature, please make sure there is a open issue that addresses the new code. 
**No Pull Requests** will be merged that do not reference an existing issue in the repo.

Please use the Android Studio formatting settings set for this project in the repo.

All strings visible to the user need to go into strings resource file. 

#### Project Goals

* Provide the best GUI git client available on any platform
* Be usable on both phone, tablet and laptop form-factor devices

#### Non-goals for the project

* Support for proprietary vendor APIs (eg. Github)

#### Major Contributions

For new features, a discussion of the new functionality may need to take place in the comments on the issue covering it, so it may be best for that to occur before you spend time on writing the new code.

The app is about to have a major restructure. All new functionality in the app will be written in Kotlin/Rx per #277. Please be aware that the project is now using Data Binding Library and all future functionality should make use of it.

#### Submitting a Pull Request (PR)
Fork from this repo, create a new branch, commit your changes and then send a pull request against the **master** branch of this repo.

If you are working on a branch for some time, you may find that changes to master get merged in the meantime, if that happens please do **NOT** merge master into your branch! Instead rebase your branch onto the current head of master.
