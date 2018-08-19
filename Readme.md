# Version 2 of MGit

This represents a complete rewrite of MGit.

Version 2 is **NOT** yet production and does not share a Git history with any version 1 branches so under NO circumstances should version 2 branches ever be merged with version 1 and vice versa. Cherry-picks where appropriate can be done.


## Supported Features

* None

The aim is to reach feature parityi where it makes sense to do so with version 1 of MGit and then to continue forward with more, improved functionality.

## Contributing

Before writing code, please read the information below on the app architecture and technology stack in use.

PRs are most welcome and should be against the "v2" branch.

### Architecture

The app is intended to embody and promote the current best practise in Android development. As such its intended to have a clean and MVVM architecture and makes use of:
* Kotlin
* RxJava
* Architecture Components (eg. LiveData)
* DataBinding
* Koin

Fragments and Dagger are explicitly *not* used.


## License

BSD 2
