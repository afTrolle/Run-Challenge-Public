package app.runchallenge.view.fragment.home

import android.content.Intent
import android.view.MenuItem
import app.runchallenge.R
import app.runchallenge.model.extensions.livedata.DefaultError
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.auth.Account
import app.runchallenge.model.data.game.Game
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.fragment.base.BaseViewModel
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    override val myToolbarSettings: MyToolbarSettings
        get() = MyToolbarSettings(
            isVisible = false
            // optionsMenu = R.menu.home_menu,
            //elevation = 0f
        )


    data class HomeData(
        val userData: Account.UserData,
        val previousGame: Game = Game()
    ) {

    }

    val liveData: MyLiveData<HomeData, DefaultError> =
        MyLiveData()

    init {
        liveData.addSource(userRepository, { data ->
            when (data) {
                Account.NotSignedIN -> changeView(R.id.action_homeFragment_to_signInFragment)
                is Account.UserData -> {
                    liveData.setForceValue {
                        HomeData(data)
                    }
                }
            }
        }, {
            changeView(R.id.action_homeFragment_to_signInFragment)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_menu_action_settings -> {
                changeView(R.id.action_homeFragment_to_preferenceFragment)
                true
            }
            R.id.home_menu_action_sign_out -> {
                userRepository.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onSettingClicked() {
        changeView(R.id.action_homeFragment_to_preferenceFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        userRepository.onActivityResult(requestCode, resultCode, data)
    }

    fun onChallengeClicked() {
        changeView(R.id.action_homeFragment_to_gameFragment_ranked)
    }

    fun onChallengeFriendClicked() {
        changeView(R.id.action_homeFragment_to_gameFragment_Invite)
    }

    fun onHistoryClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onInviteInbox() {
        changeView(R.id.action_homeFragment_to_gameFragment_Inbox)
    }


    fun onCreate() {
        userRepository.success?.let {
            if (it is Account.NotSignedIN) {
                changeView(R.id.action_homeFragment_to_signInFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        liveData.success?.let { data ->
            liveData.postValue { data }
        }
    }


}


