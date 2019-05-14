package app.runchallenge.controller.provider

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import app.runchallenge.R
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.auth.Account
import app.runchallenge.model.data.auth.AccountError
import app.runchallenge.model.data.auth.toUserData
import app.runchallenge.model.extensions.log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.games.Games


private const val RC_SIGN_IN: Int = 1001
const val RC_RESOLVE_ERROR: Int = 1002

class AccountService constructor(private val context: Context) : MyLiveData<Account, AccountError>() {


    /*
    Extension functions
     */

    private val GoogleSignInAccount?.isSignedIn: Boolean get() = this != null

    private val signInUser: GoogleSignInAccount? get() = GoogleSignIn.getLastSignedInAccount(context)

    private val signInClient: GoogleSignInClient =
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)


    override fun onActive() {
        log("onActive")
        silentSignIn()
    }


    /*
    interface
     */

    fun interactiveSignIn(fragment: Fragment) {
        if (!isWorking && !signInUser.isSignedIn) {
            val signInIntent = signInClient.signInIntent
            setWorking()
            fragment.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }


    private fun silentSignIn() {
        val user = signInUser
        if (!user.isSignedIn) {
            signInClient.silentSignIn().addOnSuccessListener { googleSignInAccount ->
                onSignIn(googleSignInAccount)
            }.addOnFailureListener {
                onNotSignedIn()
            }
        } else {
            user?.let {
                onSignIn(it)
            }
        }
    }

    fun signOut() {
        if (signInUser.isSignedIn)
            signInClient.revokeAccess().continueWith {
                //TODO maybe remove revoke Access
                signInClient.signOut().addOnCompleteListener {
                    onNotSignedIn()
                }
            }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log(" onActivityResult : resultCode $resultCode, requestCode $requestCode")
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                log(" onActivityResult : success logging in")
                result.signInAccount?.let { onSignIn(it) } ?: onUnResolvableError(null)
            } else if (result.status.hasResolution()) {
                onResolvableError(result.status)
            } else {
                onUnResolvableError(result.status)
            }

        } else if (requestCode == RC_RESOLVE_ERROR) {

            if (resultCode == Activity.RESULT_OK) {
                silentSignIn()
            } else {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                onUnResolvableError(result.status)
            }

        }
    }


/*
 *  State change GameEvents
 */

    /**
     * called when user signs-in
     */
    private fun onSignIn(signInAccount: GoogleSignInAccount) {
        log("on sign in")

        Games.getPlayersClient(context, signInAccount).currentPlayer
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result?.toUserData?.let { account ->
                        setValue {
                            account
                        }
                    }
                } else {
                    val exception = it.exception as ApiException
                    onUnResolvableError(exception.localizedMessage)
                }
            }
    }

    /**
     * called when user signed out
     */
    private fun onNotSignedIn() {
        log("on NotSign in")
        setValue {
            Account.NotSignedIN
        }
    }

    /**
     * when user failed to sign in
     */
    private fun onResolvableError(status: Status) {
        log("onResolvableError : called")
        val error = AccountError.ResolvableError(status)
        setError(error)
    }


    private fun onUnResolvableError(status: Status) {
        val message = status.statusMessage ?: context.getString(R.string.failed_to_sign_in)
        onUnResolvableError("$message  Error Code: ${status.statusCode}")
    }


    private fun onUnResolvableError(failed_to_resolve_error: Int) {
        val errorMessage = context.getString(failed_to_resolve_error)
        onUnResolvableError(errorMessage)
    }

    private fun onUnResolvableError(errorMessage: String?) {
        log("onUnResolvableError : called")
        val message = errorMessage ?: context.getString(R.string.failed_to_sign_in)
        setError(AccountError.UnResolvableError(message))
    }

}





