package com.parmenion.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.entersekt.sdk.Auth
import com.entersekt.sdk.Button
import com.entersekt.sdk.Error
import com.entersekt.sdk.Service
import com.entersekt.sdk.callback.AuthAnswerCallback
import com.entersekt.sdk.callback.SignupCallback
import com.parmenion.libraries.transakt.arch.TransaktModule
import com.parmenion.libraries.transakt.managers.TransaktManagerImpl
import com.parmenion.libraries.transakt.managers.contract.StatusConnectError
import com.parmenion.libraries.transakt.managers.contract.StatusInProgress
import com.parmenion.libraries.transakt.managers.contract.StatusStartError
import com.parmenion.libraries.transakt.managers.contract.StatusSuccess
import kotlinx.android.synthetic.main.activity_transakt.*

class TransaktActivity : AppCompatActivity() {

    val serviceID = "480a2555-c8ff-4d84-9613-82ae1f6e306b"
    val transaktManager = TransaktModule.transaktManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transakt)

        transakt_service_id.text = serviceID
        setupTransakt()
        checkRegistered()

    }

    @SuppressLint("CheckResult")
    private fun setupTransakt() {
        //todo "480a2555-c8ff-4d84-9613-82ae1f6e306b" This is the sandbox service https://saturn.entersekt.com/apidemo/ for dev only
        //username: mubaloo pass: R4pp8Jzn

        transakt_start_action.setOnClickListener {
            transaktManager.init(this, serviceID)
        }

        transaktManager.transaktStartListener()
                .onErrorResumeNext(transaktManager.transaktStartListener())
                .subscribe({ status ->
                    when (status) {
                        is StatusStartError -> {
                            Log.e("Transakt", "Start error loading Transakt ${status.error}")
                            transakt_status.text = "Unknown transakt Error"
                        }
                        is StatusConnectError -> {
                            Log.e("Transakt", "Start error connection transakt ${status.error}")
                            transakt_status.text = "Transakt Connection Error"

                        }
                        is StatusInProgress -> transakt_status.text = "In Progress"
                        is StatusSuccess -> {
                            transakt_status.text = "Successfully Connected to $serviceID"
                            checkRegistered()
                        }
                    }
                }, {
                    Log.e("Transakt", "Error", it)
                    transakt_status.text = "Unknown system Error"
                })

        transaktManager.authListener()
                .onErrorResumeNext(transaktManager.authListener())
                .subscribe({
                    if (it.isPresent) {
                        transakt_auth_title.text = it.get().title
                        transakt_auth_body.text = it.get().text
                        setButtons(it.get())
                    } else {
                        transakt_auth_title.text = "title here"
                        transakt_auth_body.text = "body here"
                        setButtons(null)
                    }
                }, {
                    Log.e("Transakt", "auth Error", it)

                })

        transakt_sign_up_action.setOnClickListener {
            val signupCode = transakt_sign_up_input.text.toString()
            transaktManager.signUpCode(signupCode, object : SignupCallback {
                override fun onSuccess(p0: Service?) {
                    transakt_sign_up_status.text = "sign Up sent Success"
                }

                override fun onError(p0: Service?, p1: Error?) {
                    transakt_sign_up_status.text = "sign Up sent Error, $p1"

                }
            })
        }
    }

    @SuppressLint("CheckResult")
    private fun checkRegistered() {
        transaktManager.registerListener()
                .onErrorResumeNext(transaktManager.registerListener())
                .subscribe({

                    transakt_reg_status.text = "Registered ${it.isRegistered}"
                    transakt_emcert.text = transaktManager.getEmCert()
                    if (it.isRegistered) {
                        transakt_content.visibility = View.VISIBLE
                    }
                }, {
                    Log.e("Transakt", "Error", it)
                })

    }

    private fun setButtons(lastAuth: Auth?) {
        if (lastAuth == null) {
            transakt_auth_pos.visibility = View.GONE
            transakt_auth_maybe.visibility = View.GONE
            transakt_auth_neg.visibility = View.GONE
        } else {

            val buttonsMap = lastAuth.buttons.associateBy { it.role }
            if (buttonsMap.containsKey(TransaktManagerImpl.ButtonRole.POSITIVE.id)) {
                setupButton(transakt_auth_pos, buttonsMap[TransaktManagerImpl.ButtonRole.POSITIVE.id]!!) {

                    sentAuth(it, lastAuth)
                }
            } else {
                transakt_auth_pos.visibility = View.GONE
            }
            if (buttonsMap.containsKey(TransaktManagerImpl.ButtonRole.NEUTRAL.id)) {
                setupButton(transakt_auth_maybe, buttonsMap[TransaktManagerImpl.ButtonRole.NEUTRAL.id]!!) {
                    sentAuth(it, lastAuth)
                }
            } else {
                transakt_auth_maybe.visibility = View.GONE
            }
            if (buttonsMap.containsKey(TransaktManagerImpl.ButtonRole.NEGATIVE.id)) {
                setupButton(transakt_auth_neg, buttonsMap[TransaktManagerImpl.ButtonRole.NEGATIVE.id]!!) {
                    sentAuth(it, lastAuth)
                }
            } else {
                transakt_auth_neg.visibility = View.GONE
            }
        }

    }

    private fun setupButton(view: TextView, button: Button, onClick: (button: Button) -> Unit) {
        view.visibility = View.VISIBLE
        view.text = button.label
        view.setOnClickListener {
            onClick(button)
        }
    }

    private fun sentAuth(button: Button, auth: Auth) {
        button.select()

        transaktManager.sendAuthAnswer(auth, object : AuthAnswerCallback {
            override fun onSuccess(service: Service?, auth: Auth?) {
                transakt_send_status.text = "Successfully sent Auth"
            }

            override fun onError(service: Service?, error: Error?, auth: Auth?) {
                transakt_send_status.text = "Failed to send Auth $error"

            }
        })
    }

}
