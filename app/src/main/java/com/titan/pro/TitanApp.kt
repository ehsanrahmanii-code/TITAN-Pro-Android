package com.titan.pro

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class TitanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }
}
