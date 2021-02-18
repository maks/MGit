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

### 克隆远程存储库

1. 点击 `+` 图标添加新的存储库
2. 输入远程URL (参閲下面的 URL 格式)
3. 输入本地存储库名称 - 请注意这 **不是** 完整路径, 因为MGit将所有存储库存储在同一本地目录中 (可以在MGit设置中更改)
4. 点击 `克隆` 按钮
5. 如果需要, 系统将提示您输入连接到远程repo的凭据. MGit将把存储库（所有分支）下载到您的设备上

### 创建本地存储库
1. Click on the `+` icon to add a new repository
2. Click on `Init Local` to create a local repository
3. Enter the name for this repository when prompted
4. A local empty repo will be created

### URL 格式

#### SSH URLs

* SSH 在标准端口(22)上运行: `ssh://username@server_name/path/to/repo`
* SSH 在非标准端口上运行: `ssh://username@server_name:port/path/to/repo`
* `username` 是默认情况下必需的, MGit 尝试以 root 用户身份连接.

#### HTTP(S) URLs

* HTTP(S) URL: `https://server_name/path/to/repo`

## 待办事项列表

[在Github上跟踪未来的增强和bugs](https://github.com/maks/MGit/issues).

## 许可

参閲 [GPLv3](./LICENSE)

所有代码出自于 `maks@manichord.com` 也可以在 [MIT license](https://en.wikipedia.org/wiki/MIT_License)下使用.

## 幫助

如果你想帮助改善这个项目, 贡献, 特别是翻译是非常欢迎的. 此外通过維基为本提供文档合并也是最受欢迎的!

### 贡献代码
如果你想贡献代码，无论是一个错误修复或一个新的功能，，。不会合并。
如果你想贡献代码, 无论是一个错误修复或一个新的功能, 请确保有一个解决新的代码的问题. **No Pull Requests** 不引用合并當中现有问题的请求.

请使用repo中为此项目设置的Android Studio格式设置.

所有对用户可见的字符串都需要进入字符串资源文件. 

#### 项目目标

* 提供在任何平台上可用的最佳帶GUI-Git客户端
* 可用于电话、平板电脑和笔记本电脑等設備

#### 非目标项目

* 支持专有供应商APIs (eg. Github)

#### 主要贡献

对于新特性, 新功能的讨论可能需要在涉及它的问题的注释中进行, 因此最好在您花时间编写新代码之前进行.

该应用程序即将进行重大重组. 用 Kotlin/Rx 应用程序中的所有新功能都将按照 #277 编写. 请注意该项目现在正在使用数据绑定库，所有未来的功能都应该使用它.

#### 提交拉取请求 (PR)

从这个repo分支, 创建一个新分支, 提交您的更改后发送一个对这个repo的 **master** 分支的请求.

如果您在一个分支上工作了一段时间, 您可能会发现对 master 的更改同时被合并, 如果发生这种情况，请 **不要** 将 master 合并到您的分支中! 相反将你的分支重设为当前的 master 分支.
