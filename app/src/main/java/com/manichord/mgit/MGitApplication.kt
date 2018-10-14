package com.manichord.mgit

import android.app.Application
import com.bugsnag.android.Bugsnag

class MGitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this)
    }
}
