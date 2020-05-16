package com.parmenion.libraries.tokenstorage.manager

import android.content.Context
import app.raybritton.tokenstorage.RxTokenStorage
import app.raybritton.tokenstorage.crypto.CertCrypto
import app.raybritton.tokenstorage.keyCrypto.NoKeyCrypto
import app.raybritton.tokenstorage.persistence.PreferencesPersistence
import com.parmenion.libraries.tokenstorage.manager.contract.TokenManager

class TokenManagerImpl constructor(private val context: Context) : TokenManager {


    private val accessToken by lazy {
        tokenStorage.wrap("access_Token.encrypted_string")
    }

    private val refreshToken by lazy {
        tokenStorage.wrap("refresh_token.encrypted_string")
    }

    private val accessTokenExpirationDateTime by lazy {
        tokenStorage.wrap("access_Token_expiration_datetime.encrypted_string")
    }

    private val tokenStorage by lazy {
        RxTokenStorage(
            CertCrypto.defaults(context),
            PreferencesPersistence(context, "parmenion-token-${context.packageName}.pref"),
            NoKeyCrypto()
        )
    }

    override fun getAccessToken(): String? = accessToken.loadSync()

    override fun setAccessToken(accessToken: String) = this.accessToken.saveSync(accessToken)

    override fun getRefreshToken(): String? = refreshToken.loadSync()

    override fun setRefreshToken(refreshtoken: String) = refreshToken.saveSync(refreshtoken)
    override fun getAccessTokenExpirationDateTime(): String? =
        accessTokenExpirationDateTime.loadSync()

    override fun setAccessTokenExpirationDateTime(dateTime: String) =
        this.accessTokenExpirationDateTime.saveSync(dateTime)

    override fun clearAllTokens() {
        tokenStorage.clearAllSync()
    }

}