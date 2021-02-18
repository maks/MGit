# MGit

[English](README.md) | [简体中文](README_CN.md)

MGit 是一个安卓上的Git客户端应用.

这是 [SGit 项目](https://github.com/sheimi/SGit)的开发延续.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="去Google商店下载"
      height="80">](https://play.google.com/store/apps/details?id=com.manichord.mgit)
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="去F-Droid下载"
      height="80">](https://f-droid.org/packages/com.manichord.mgit)

## 注意

[![构建状态](https://travis-ci.org/maks/MGit.svg?branch=master)](https://travis-ci.org/maks/MGit)

[![加入 https://gitter.im/MGit-Android/Lobby 的讨论](https://badges.gitter.im/MGit-Android/Lobby.svg)](https://gitter.im/MGit-Android/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![翻译 - 使用Stringlate](https://img.shields.io/badge/translate%20with-stringlate-green.svg)](https://lonamiwebs.github.io/stringlate/translate?git=https%3A%2F%2Fgithub.com%2Fmaks%2FMGit)

* 如果您遇到任何问题 (bugs, crashes, etc.) 并希望帮助改进此项目, 请在打开[GitHub上的issue](https://github.com/maks/MGit/issues/new) 描述问题: 问题是什么; 以及它们是如何引起的, 以便重新创建和修复bugs.
* 这个应用程序需要 Android v5.0 以上版本

### 编辑文件

从 15.7 版本开始, MGit 不再提供内部文本编辑器, 如果您希望编辑文件, 则需要安装编辑器应用程序. 

一个经过测试可以与MGit一起工作的开源编辑器是["Turbo Editor"]( https://play.google.com/store/apps/details?id=com.maskyn.fileeditorpro)

但是其他支持文件提供者的也应该起作用.

## 支持特征

* 创建本地存储库
* 克隆远程存储库
* 拉取目的源
* 删除本地存储库
* 浏览文件
* 浏览提交说明 (短的)
* 切换分支和标记
* 支持 HTTP/HTTPS/SSH (包括带有私钥密码的SSH)
* 支持用户名/密码验证
* 搜索本地存储库
* 私钥管理
* 手动选择代码语言
* `git diff` 對比提交
* 导入现有存储库 (也就是说, 您可以从计算机复制存储库并导入到MGit)
* 切换远程分支
* 合并分支
* 推送合并内容
* 编辑文件 (可编辑擴展文件类型的内置编辑器或外部应用程序)
* 提交并推送更改的文件
* 提交者信息
* 提示输入密码
* *Option* 上可保存用户名/密码
* `git status`
* 克隆时取消
* 在阶段添加修改文件
* 查看暂存文件的状态 (亦称索引)
* `git rebase`
* `git cherrypick`
* `git checkout <file>` (重置文件的更改)

## 快速启动

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
