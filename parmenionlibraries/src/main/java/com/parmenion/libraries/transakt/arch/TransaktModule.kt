package com.parmenion.libraries.transakt.arch

import com.parmenion.libraries.transakt.managers.TransaktManagerImpl

object TransaktModule {
    val transaktManager by lazy {
        TransaktManagerImpl()
    }
}
