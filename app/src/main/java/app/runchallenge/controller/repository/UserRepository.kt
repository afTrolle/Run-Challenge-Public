package app.runchallenge.controller.repository

import android.content.Intent
import androidx.fragment.app.Fragment
import app.runchallenge.controller.provider.AccountService
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.auth.Account
import app.runchallenge.model.data.auth.AccountError


//repositories can depend on other repositories
//however a service can only be dependent on once.
class UserRepository constructor(private val accountService: AccountService) : MyLiveData<Account, AccountError>() {

    init {
        addSource(accountService, { state ->
            setForceSuccess(state)
        }, { error ->
            setError(error)
        })
    }


    /*
     * Interface functions
     */

    public fun signIn(fragment: Fragment) {
        accountService.interactiveSignIn(fragment)
    }

    public fun signOut() {
        accountService.signOut()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        accountService.onActivityResult(requestCode, resultCode, data)
    }

}

