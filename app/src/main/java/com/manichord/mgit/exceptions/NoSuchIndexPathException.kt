package com.manichord.mgit.exceptions

@Suppress("UNUSED") // The additional constructors should be there once they are used.
class NoSuchIndexPathException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
