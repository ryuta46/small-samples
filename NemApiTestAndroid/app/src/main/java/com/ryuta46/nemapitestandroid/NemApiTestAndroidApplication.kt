package com.ryuta46.nemapitestandroid

import android.app.Application
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security


class NemApiTestAndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Security.addProvider(BouncyCastleProvider())
    }

    override fun onTerminate() {
        Security.removeProvider(BouncyCastleProvider().name)
        super.onTerminate()
    }
}