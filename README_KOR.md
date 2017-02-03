
안드로이드에서 구동되는 Git 플랫폼입니다.

This is a continuation of the SGit project (https://github.com/sheimi/SGit)

## Notes

[![Build Status](https://travis-ci.org/maks/MGit.svg?branch=master)](https://travis-ci.org/maks/MGit)

[![Join the chat at https://gitter.im/MGit-Android/Lobby](https://badges.gitter.im/MGit-Android/Lobby.svg)](https://gitter.im/MGit-Android/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

 * If you encounter any issues (bugs, crashes, etc.) and want to help improve this project, please open an issue on [GitHub](https://github.com/maks/MGit/issues/new) describing: what the issues are; and how they were caused, to allow for re-creation and fixing of bugs.
 * 이 앱을 실행하기 위해 최소 안드로이드 4.x 이상이 필요합니다.

## 기능

* 로컬 repository 생성
* 원격 repository 가져오기
* origin에서 가져오기
* 로컬 repository 제거
* 파일 탐색
* 커밋 메세지 탐색하기 (short)
* branch와 태그 checkout
* HTTP/HTTPS/SSH 지원 (private key passphrase 없는 SSH)
* Username/Password authentication is supported
* 로컬 repository 검색
* Private key 관리
* Manually choose code language
* `git diff` between commits (to be enhanced)
* Import copied repositories (that is, you can copy a repository from computer and import to SGit)
* Checkout remote branches
* branch 병합
* Push merged content
* Edit file (you must have some app that can edit file)
* Commit and push changed files (commit all changes)
* Committer information
* Prompt for password
* Choose not to save password and username (will not be saved in disk but may be temporarily saved in memory)
* `git status`
* Cancel when cloning
* Add modified file to stage
* `git rebase`
* `git cherrypick`
* `git checkout <file>` (reset changes of a file)

<a href="https://play.google.com/store/apps/details?id=com.manichord.mgit"><img alt="Android app on Google Play" src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" /></a>

## 빠른 시작

### 원격 repository 클론하기

1. Click on the *+* icon to add a new repository.
2. Enter remote URL (see URL format below).
3. Enter local repository name - note that this is not a full path since SGit stores all repositories in the same directory on the mobile device.
4. Username - username to use to clone the remote repo.
5. Password - password to use to clone the remote repo.
6. Click the *Clone* button.
7. If all the credentials are correct, SGit will download the repository (all branches) to your device.

### Create a local repository
1. Click on the *+* icon to add a new repository.
2. Click on *Init Local* to create a local repository.
3. Enter the name for this repository when prompted.
4. A local repo will be created.

### URL 포맷

#### SSH URLs

 * SSH running on standard port (22): `ssh://username@server_name/path/to/repo`
* SSH running on non-standard port: `ssh://username@server_name:port/path/to/repo`
* `username` is needed - by default, SGit tries to connect as root.

#### HTTP(S) URLs

* HTTP(S) URL: `https://server_name/path/to/repo`

## To Do List

[Future enhancements are tracked on Github](https://github.com/maks/MGit/issues)

## 라이센스

[GPLv3](./LICENSE)

## Help

If you want to help improve this project, contributions, especially translations are very welcome.

Fork from this repo, create a new branch, commit your changes and then send a pull request against the **master** branch of this repo.
