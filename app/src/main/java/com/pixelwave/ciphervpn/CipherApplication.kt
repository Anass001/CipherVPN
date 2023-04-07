package com.pixelwave.ciphervpn

import android.app.Application
import android.content.Context
import com.pixelwave.ciphervpn.data.Repository
import com.pixelwave.ciphervpn.data.db.DatabaseBuilder
import com.pixelwave.ciphervpn.data.remote.RetrofitClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CipherApplication : Application()