package com.parmenion.libraries.biometrics.util


sealed class BiometricResult {
    class Success() : BiometricResult()
    data class Fail(val message: String) : BiometricResult()
    data class Error(val errCode: Int,val message: String) : BiometricResult()
}

