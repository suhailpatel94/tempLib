package com.parmenion.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.stetho.Stetho
import com.parmenion.libraries.biometrics.BiometricsInitializeCallback
import com.parmenion.libraries.biometrics.FingerPrintAuthenticationResult
import com.parmenion.libraries.biometrics.util.BiometricResult
import com.parmenion.libraries.biometrics.util.BiometricResultCallback
import com.parmenion.libraries.biometrics.util.BiometricsUtil
import com.parmenion.libraries.pinstorage.manager.PinManagerImpl
import com.parmenion.libraries.tokenstorage.manager.TokenManagerImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val pinManager = PinManagerImpl(this)
    val tokenManager = TokenManagerImpl(this)
    val context: Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fingerPrintSetup()
        test_fingerprint_action.setOnClickListener {
            fingerPrintSetup()
        }
        pinSetup()
        tokenSetup()
        test_transakt.setOnClickListener {
            startActivity(Intent(this, TransaktActivity::class.java))
        }
        scanFingerPrint.setOnClickListener {
            showScanDialog()
        }
        Stetho.initializeWithDefaults(application)

    }

    @SuppressLint("CheckResult")
    fun fingerPrintSetup() {
        BiometricsUtil.initializeBiometrics(this, object : BiometricsInitializeCallback {
            override fun success() {
                test_fingerprint_log.text = "Success loading finger print"
            }

            override fun message(message: String) {
                test_fingerprint_log.text = "Error with finger print: ${message}"
            }

            override fun messageException(throwable: Throwable, message: String) {
                Log.e("Fingerprint", "fingerprint Exception", throwable)

                test_fingerprint_log.text = "BAD error with finger print: ${message}"
            }
        })

        val fingerprintScanAvailable = BiometricsUtil.isFingerprintScanAvailable()

        if (fingerprintScanAvailable) {
            BiometricsUtil.authenticateFingerPrint()
                .subscribe({
                    when (it) {
                        is FingerPrintAuthenticationResult.AuthenticationSuccess -> test_fingerprint_status.text =
                            "Success"
                        is FingerPrintAuthenticationResult.AuthenticationNonFatalError -> test_fingerprint_status.text =
                            it.errorMessage
                        is FingerPrintAuthenticationResult.AuthenticationFatalError -> test_fingerprint_status.text =
                            it.errorMessage
                    }
                }, {
                    Log.e("Fingerprint", "fingerprint is having a bad time", it)
                    test_fingerprint_status.text = "BAD error with finger print: "
                })
        } else {
            test_fingerprint_status.text =
                "fingerprint Not Available, either no hardware or finger not registered"
        }
    }

    fun pinSetup() {
        test_pin_pin.text = if (pinManager.hasPinSet()) "Has pin" else "Unset"

        test_pin_action.setOnClickListener {
            if (pinManager.hasPinSet()) {
                val doesPinMatch = pinManager.doesPinMatch(test_pin_input.text.toString())
                test_pin_pin.text = if (doesPinMatch) "pins matches" else "do not match"
            } else {
                test_pin_pin.text = test_pin_input.text
                pinManager.setPin(test_pin_input.text.toString())
            }
        }
        test_pin_action_clear.setOnClickListener {
            test_pin_pin.text = "cleared"
            pinManager.clearPin()
        }
    }

    fun tokenSetup() {
        test_token_access_current.text =
            if (tokenManager.getAccessToken().isNullOrBlank()) "token set to ${tokenManager.getAccessToken()}" else "Unset"
        test_token_refresh_current.text =
            if (tokenManager.getRefreshToken().isNullOrBlank()) "token set to ${tokenManager.getRefreshToken()}" else "Unset"

        test_token_access_action.setOnClickListener {
            val input = test_token_access_input.text.toString()
            tokenManager.setAccessToken(input)
            test_token_access_current.text =
                "input ${input} , save as ${tokenManager.getAccessToken()}"
        }

        test_token_refresh_action.setOnClickListener {
            val input = test_token_refresh_input.text.toString()
            tokenManager.setRefreshToken(input)
            test_token_refresh_current.text =
                "input ${input} , save as ${tokenManager.getRefreshToken()}"
        }
        test_token_clear_all_action.setOnClickListener {
            tokenManager.clearAllTokens()
            test_token_access_current.text = "token set to ${tokenManager.getAccessToken()}"
            test_token_refresh_current.text = "token set to ${tokenManager.getRefreshToken()}"

        }

    }

    fun showScanDialog() {
        BiometricsUtil.showBiomertricDialog(this, object : BiometricResultCallback {
            override fun result(biometricResult: BiometricResult) {
                when (biometricResult) {
                    is BiometricResult.Success -> {
                        Toast.makeText(context, "Authenticaion Successful", Toast.LENGTH_SHORT)
                            .show();
                    }
                    is BiometricResult.Fail -> {
                        Toast.makeText(context, "Authenticaion Failed", Toast.LENGTH_SHORT).show();
                    }
                    is BiometricResult.Error -> {
                        Toast.makeText(
                            context,
                            "Authenticaion Error: " + biometricResult.message,
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            }
        })
    }

}
