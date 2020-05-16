package com.parmenion.libraries.biometrics

sealed class FingerPrintAuthenticationResult {
    class AuthenticationSuccess : FingerPrintAuthenticationResult()
    class AuthenticationNonFatalError(val errorMessage: String) : FingerPrintAuthenticationResult()
    class AuthenticationFatalError(val errorMessage: String) : FingerPrintAuthenticationResult()
}