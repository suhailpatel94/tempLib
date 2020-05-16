package com.parmenion.libraries.tokenstorage.manager.contract

interface TokenManager {
    fun getAccessToken(): String?
    fun setAccessToken(accessToken: String)
    fun getRefreshToken(): String?
    fun setRefreshToken(refreshtoken: String)
    fun getAccessTokenExpirationDateTime(): String?
    fun setAccessTokenExpirationDateTime(dateTime: String)
    fun clearAllTokens()
}