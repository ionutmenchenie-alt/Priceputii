package ro.priceputii.app.ui.onboarding

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import ro.priceputii.app.data.Roles
import ro.priceputii.app.data.ServiceLocator
import ro.priceputii.app.data.normalizeRoPhone

class LoginViewModel(handle: SavedStateHandle) : ViewModel() {

    enum class Step { DETAILS, CODE, NAMES_ONLY }

    private val auth = ServiceLocator.auth
    val role: String = handle["role"] ?: Roles.CLIENT

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var avatar by mutableStateOf("")
    var phone by mutableStateOf("")
    var code by mutableStateOf("")
    var step by mutableStateOf(if (auth.currentUid != null) Step.NAMES_ONLY else Step.DETAILS)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Android read the SMS by itself.
            signIn(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            busy = false
            error = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Numărul de telefon nu e valid."
                is FirebaseTooManyRequestsException -> "Prea multe încercări. Mai încearcă peste puțin timp."
                else -> "Nu am putut trimite codul: ${e.localizedMessage}"
            }
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            resendToken = token
            busy = false
            step = Step.CODE
        }
    }

    private fun namesValid(): Boolean {
        if (firstName.isBlank() || lastName.isBlank()) {
            error = "Completează numele și prenumele."
            return false
        }
        if (avatar.isBlank()) {
            error = "Alege un avatar."
            return false
        }
        return true
    }

    fun sendCode(activity: Activity) {
        error = null
        if (!namesValid()) return
        val e164 = normalizeRoPhone(phone)
        if (e164 == null) {
            error = "Scrie un număr de mobil românesc, de exemplu 0722 123 456."
            return
        }
        busy = true
        auth.sendCode(activity, e164, resendToken, callbacks)
    }

    fun confirmCode() {
        error = null
        val id = verificationId ?: return
        if (code.trim().length != 6) {
            error = "Codul are 6 cifre."
            return
        }
        signIn(auth.credential(id, code.trim()))
    }

    fun changeNumber() {
        code = ""
        error = null
        step = Step.DETAILS
    }

    fun completeRegistration() {
        error = null
        if (!namesValid()) return
        viewModelScope.launch {
            busy = true
            runCatching { register() }.onFailure {
                busy = false
                error = "Nu am putut salva profilul: ${it.localizedMessage}"
            }
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            busy = true
            runCatching {
                auth.signIn(credential)
                register()
            }.onFailure {
                busy = false
                error = if (it is FirebaseAuthInvalidCredentialsException) {
                    "Codul nu e corect."
                } else {
                    "Nu am putut intra în cont: ${it.localizedMessage}"
                }
            }
        }
    }

    /** Once the profile exists, the app moves on by itself. */
    private suspend fun register() {
        val uid = auth.currentUid ?: return
        ServiceLocator.users.register(uid, firstName.trim(), lastName.trim(), avatar, role)
    }
}
