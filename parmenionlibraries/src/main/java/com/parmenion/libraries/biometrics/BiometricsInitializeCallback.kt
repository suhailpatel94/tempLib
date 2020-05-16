package com.parmenion.libraries.biometrics

interface BiometricsInitializeCallback {
    fun success()
    fun message(message: String)
    fun messageException(throwable: Throwable, message: String)
}