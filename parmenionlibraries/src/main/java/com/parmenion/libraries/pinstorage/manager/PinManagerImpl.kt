package com.parmenion.libraries.pinstorage.manager

import android.content.Context
import app.raybritton.tokenstorage.RxTokenStorage
import app.raybritton.tokenstorage.crypto.CertCrypto
import app.raybritton.tokenstorage.keyCrypto.NoKeyCrypto
import app.raybritton.tokenstorage.persistence.PreferencesPersistence
import com.parmenion.libraries.pinstorage.manager.contract.PinManager

class PinManagerImpl constructor(private val context: Context) : PinManager {

    private val pinPreference by lazy {
        tokenStorage.wrap("pin.encrypted_string")
    }

    private val tokenStorage by lazy {
        RxTokenStorage(
            CertCrypto.defaults(context),
            PreferencesPersistence(context, "parmenion-pin-${context.packageName}.pref"),
            NoKeyCrypto()
        )
    }

    override fun hasPinSet(): Boolean {
        return pinPreference.isSetSync()
    }

    override fun doesPinMatch(pin: String): Boolean {
        return pinPreference.loadSync() == pin
    }

    override fun setPin(pin: String) {
        pinPreference.saveSync(pin)
    }

    override fun clearPin() {
        pinPreference.clearSync()
    }

}