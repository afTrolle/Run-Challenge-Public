package app.runchallenge.view.fragment.sign.`in`

import android.content.Intent
import androidx.fragment.app.Fragment
import app.runchallenge.R
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.auth.Account
import app.runchallenge.model.data.auth.AccountError
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.fragment.base.BaseViewModel
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    override val myToolbarSettings get() = MyToolbarSettings(isVisible = false)

    val liveData: MyLiveData<Account, AccountError> =
        MyLiveData()

    init {
        liveData.addSource(userRepository, { state ->
            when (state) {
                is Account.UserData -> {
                    changeView(R.id.action_signInFragment_to_homeFragment)
                }
            }
        }, { error ->
            liveData.setError(error)
        })
    }


    fun onSignInClicked(fragment: Fragment) {
        userRepository.signIn(fragment)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        userRepository.onActivityResult(requestCode, resultCode, data)
    }


}