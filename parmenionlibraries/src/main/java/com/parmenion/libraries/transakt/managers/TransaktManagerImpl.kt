package com.parmenion.libraries.transakt.managers

import android.content.Context
import com.entersekt.sdk.AppMultifactor
import com.entersekt.sdk.Auth
import com.entersekt.sdk.ConnectionContext
import com.entersekt.sdk.Error
import com.entersekt.sdk.Info
import com.entersekt.sdk.Notify
import com.entersekt.sdk.Otp
import com.entersekt.sdk.Service
import com.entersekt.sdk.Signup
import com.entersekt.sdk.TData
import com.entersekt.sdk.TransaktSDK
import com.entersekt.sdk.callback.AuthAnswerCallback
import com.entersekt.sdk.callback.ConnectCallback
import com.entersekt.sdk.callback.OtpCallback
import com.entersekt.sdk.callback.SignupCallback
import com.entersekt.sdk.callback.TransaktSDKCallback
import com.entersekt.sdk.listener.ConnectionListener
import com.entersekt.sdk.listener.RegisterListener
import com.parmenion.libraries.transakt.AuthOptional
import com.parmenion.libraries.transakt.managers.contract.StatusConnectError
import com.parmenion.libraries.transakt.managers.contract.StatusInProgress
import com.parmenion.libraries.transakt.managers.contract.StatusStartError
import com.parmenion.libraries.transakt.managers.contract.StatusSuccess
import com.parmenion.libraries.transakt.managers.contract.TransaktInitStatus
import com.parmenion.libraries.transakt.managers.contract.TransaktManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber
import kotlin.concurrent.thread

class TransaktManagerImpl : TransaktManager {

    private lateinit var transaktSDK: TransaktSDK
    private lateinit var service: Service
    private lateinit var serviceId: String

    override val lastAuthSubject: ReplaySubject<AuthOptional<Auth>> = ReplaySubject.createWithSize(1)
    override val lastRegSubject: ReplaySubject<Service> = ReplaySubject.createWithSize(1)
    override val transaktStartSuccessSubject: ReplaySubject<TransaktInitStatus> = ReplaySubject.createWithSize(1)

    override val connectionListener: Flowable<ConnectionContext> =
        Flowable.create<ConnectionContext>({
            TransaktSDK.setConnectionListener(object : ConnectionListener {
                override fun onConnected(connectionContext: ConnectionContext) {
                    it.onNext(connectionContext)
                }

                override fun onDisconnected(connectionContext: ConnectionContext) {
                    it.onNext(connectionContext)
                }
            })
        }, BackpressureStrategy.LATEST)


    override val notifyListener: Flowable<Notify> =
        Flowable.create<Notify>({
            TransaktSDK.setNotifyListener { _, notify -> it.onNext(notify) }
        }, BackpressureStrategy.LATEST)

    override val tDataListener: Flowable<TData> =
        Flowable.create<TData>({
            TransaktSDK.setTDataListener { _, tData -> it.onNext(tData) }
        }, BackpressureStrategy.LATEST)

    override val appMultifactorListener: Flowable<AppMultifactor> =
        Flowable.create<AppMultifactor>({
            TransaktSDK.setAppMultifactorListener { appMultifactor, _ -> it.onNext(appMultifactor) }
        }, BackpressureStrategy.LATEST)

    override val isRooted: Boolean
        get() = TransaktSDK.getInfo().osPrivilege == Info.OsPrivilege.ROOT


    override fun init(context: Context, serviceId: String?) {
        if (this::serviceId.isInitialized.not() && serviceId != null) {
            this.serviceId = serviceId
        }

        if (transaktStartSuccessSubject.hasValue()) {
            if (transaktStartSuccessSubject.value is StatusSuccess) {
                transaktStartSuccessSubject.onNext(StatusSuccess())//republish Success result
                return
            }
        }

        transaktStartSuccessSubject.onNext(StatusInProgress())

        thread {
            TransaktSDK.start(context, object : TransaktSDKCallback {
                override fun onReady(sdk: TransaktSDK) {
                    transaktSDK = sdk
                    connectSdkToTransakt(this@TransaktManagerImpl.serviceId)
                }

                override fun onError(error: com.entersekt.sdk.Error) {
                    transaktStartSuccessSubject.onNext(StatusStartError(error))
                }
            })

            startAuthRegListeners()
        }
    }

    private fun connectSdkToTransakt(serviceId: String) {
        transaktSDK.connect(object : ConnectCallback {
            override fun onSuccess() {
                service = transaktSDK.getService(serviceId)
                lastRegSubject.onNext(service)
                transaktStartSuccessSubject.onNext(StatusSuccess())
            }

            override fun onError(error: com.entersekt.sdk.Error) {
                transaktStartSuccessSubject.onNext(StatusConnectError(error))
            }
        })
    }

    private fun startAuthRegListeners() {
        TransaktSDK.setAuthListener { service, auth ->

            lastAuthSubject.onNext(AuthOptional.ofNullable(auth))
//            auth.id// internal ID
//
//            auth.title // title of the auth
//
//            auth.text // text of the auth
//
//            auth.textBoxes // a list of objects that can have
//            //    String getLabel();
//            //    String getText();
//            //    int getMinSize();
//            //    int getMaxSize();
//            //    List<String> getConstraints(); //Location
//            //    void setUserResponse(@NonNull String var1); // user input to the text box
//            //    String getUserResponse();
//
//            auth.nameValues // a list of names "text" and values "text"
//
//            auth.buttons // three buttons

            Timber.d("$$$ Got Auth $auth for Service $service")
        }
        TransaktSDK.setRegisterListener(object : RegisterListener {
            override fun onUnregister(service: Service) {
                Timber.d("&&& Unregistered with the service name ${service.name} , serviceId: ${service.serviceId}")
                lastRegSubject.onNext(service)
            }

            override fun onRegister(service: Service) {
                Timber.d("&&& Registered with the service name ${service.name} , serviceId: ${service.serviceId}")
                lastRegSubject.onNext(service)
            }
        })
    }

    override fun isRegistered(): Boolean {
        return when {
            this::service.isInitialized -> service.isRegistered
            else -> false
        }
    }

    override fun destroy(context: Context) {
        TransaktSDK.destroy(context)
    }

    override fun signUpCode(code: String, signupCallback: SignupCallback) {
        val signupObject = Signup()
        signupObject.signupCode = code

        service.signup(signupObject, signupCallback)
    }

    override fun sendAuthAnswer(auth: Auth, authAnswerCallback: AuthAnswerCallback) {
        transaktSDK.sendAuthAnswer(auth, object : AuthAnswerCallback {
            override fun onSuccess(service: Service?, auth: Auth?) {
                authAnswerCallback.onSuccess(service, auth)
                lastAuthSubject.onNext(AuthOptional.empty())
            }

            override fun onError(service: Service?, error: Error?, auth: Auth?) {
                authAnswerCallback.onError(service, error, auth)
                lastAuthSubject.onNext(AuthOptional.empty())
            }
        })
    }

    override fun requestOTPCallback(): Single<Otp> {
        return Single.create {
            service.getOtp(object : OtpCallback {
                override fun onSuccess(service: Service, otp: Otp) {
                    it.onSuccess(otp)
                }

                override fun onError(service: Service, error: Error) {
                    Timber.e(error.name)
                    it.onError(IllegalStateException(error.name))
                }
            })
        }
    }

    override fun authListener(): Flowable<AuthOptional<Auth>> =
        lastAuthSubject.toFlowable(BackpressureStrategy.LATEST).share()

    override fun registerListener(): Flowable<Service> {
        if (this::service.isInitialized) {
            lastRegSubject.onNext(service)
        }
        return lastRegSubject.toFlowable(BackpressureStrategy.LATEST).share()
    }

    override fun transaktStartListener(): Flowable<TransaktInitStatus> =
        transaktStartSuccessSubject.toFlowable(BackpressureStrategy.LATEST).share()

    override fun getEmCert(): String? = service.emCert.emCertId


    override fun setFCMToken(token: String) {
        TransaktSDK.getConfig().googleCloudMessagingId = token
    }

    enum class ButtonRole(val id: String) {
        POSITIVE("positive"), NEGATIVE("negative"), NEUTRAL("neutral")
    }
}