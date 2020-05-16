package com.parmenion.libraries.transakt.managers.contract

import android.content.Context
import com.entersekt.sdk.AppMultifactor
import com.entersekt.sdk.Auth
import com.entersekt.sdk.ConnectionContext
import com.entersekt.sdk.Notify
import com.entersekt.sdk.Otp
import com.entersekt.sdk.Service
import com.entersekt.sdk.TData
import com.entersekt.sdk.callback.AuthAnswerCallback
import com.entersekt.sdk.callback.SignupCallback
import com.parmenion.libraries.transakt.AuthOptional

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject

interface TransaktManager {

    val lastAuthSubject: ReplaySubject<AuthOptional<Auth>>
    val lastRegSubject: ReplaySubject<Service>
    val transaktStartSuccessSubject: ReplaySubject<TransaktInitStatus>

    val connectionListener: Flowable<ConnectionContext>

    val notifyListener: Flowable<Notify>

    val tDataListener: Flowable<TData>

    val appMultifactorListener: Flowable<AppMultifactor>

    val isRooted: Boolean

    fun init(context: Context, serviceId: String?)

    fun destroy(context: Context)

    fun signUpCode(code: String, signupCallback: SignupCallback)

    fun sendAuthAnswer(auth: Auth, authAnswerCallback: AuthAnswerCallback)

    fun isRegistered(): Boolean

    fun authListener(): Flowable<AuthOptional<Auth>>

    fun registerListener(): Flowable<Service>

    fun requestOTPCallback(): Single<Otp>

    fun getEmCert(): String?
    fun setFCMToken(token: String)
    fun transaktStartListener(): Flowable<TransaktInitStatus>
}

sealed class TransaktInitStatus

class StatusStartError(val error: com.entersekt.sdk.Error) : TransaktInitStatus()

class StatusConnectError(val error: com.entersekt.sdk.Error) : TransaktInitStatus()

class StatusSuccess() : TransaktInitStatus()

class StatusInProgress() : TransaktInitStatus()

