package com.parmenion.libraries.biometrics.util

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.ajalt.reprint.core.AuthenticationResult
import com.github.ajalt.reprint.core.Reprint
import com.github.ajalt.reprint.rxjava2.RxReprint
import com.parmenion.libraries.biometrics.BiometricsInitializeCallback
import com.parmenion.libraries.biometrics.FingerPrintAuthenticationResult
import com.parmenion.libraries.biometrics.FingerPrintAuthenticationResult.AuthenticationFatalError
import com.parmenion.libraries.biometrics.FingerPrintAuthenticationResult.AuthenticationNonFatalError
import com.parmenion.libraries.biometrics.FingerPrintAuthenticationResult.AuthenticationSuccess
import io.reactivex.Flowable
import java.util.concurrent.Executor

object BiometricsUtil {

    fun initializeBiometrics(context: Context, messageCallback: BiometricsInitializeCallback) {
        Reprint.initialize(context, object : Reprint.Logger {
            override fun log(message: String) {
                messageCallback.message(message)
            }

            override fun logException(throwable: Throwable, message: String) {
                messageCallback.messageException(throwable, message)
            }
        })
        messageCallback.success()
    }

    fun authenticateFingerPrint(): Flowable<FingerPrintAuthenticationResult> {
        return RxReprint.authenticate()
            .map {
                when (it.status) {
                    AuthenticationResult.Status.SUCCESS ->
                        AuthenticationSuccess()
                    AuthenticationResult.Status.NONFATAL_FAILURE ->
                        AuthenticationNonFatalError(it.errorMessage.toString())
                    AuthenticationResult.Status.FATAL_FAILURE ->
                        AuthenticationFatalError(it.errorMessage.toString())
                    else -> throw IllegalStateException("Reprint returned an unsupported status: ${it.status}")
                }
            }
    }

    fun isFingerprintScanAvailable() =
        Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()

    private fun verfiyingBioMetricExistence(context: Context): Pair<Boolean, String> {
        val biometricManager = BiometricManager.from(context)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                return Pair(true, "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                return Pair(false, "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                return Pair(false, "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                return Pair(
                    false,
                    "The user hasn't associated any biometric credentials with their account."
                )
            else ->
                return Pair(false, "")

        }
    }

    fun showBiomertricDialog(
        fragmentActivity: FragmentActivity,
        callback: BiometricResultCallback
    ) {

        lateinit var executor: Executor
        lateinit var biometricPrompt: BiometricPrompt
        lateinit var promptInfo: BiometricPrompt.PromptInfo

        executor =
            ContextCompat.getMainExecutor(fragmentActivity.applicationContext)
        biometricPrompt = BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.result(BiometricResult.Error(errorCode, errString.toString()))

                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    callback.result(BiometricResult.Success())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.result(BiometricResult.Fail(""))
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)

    }


}