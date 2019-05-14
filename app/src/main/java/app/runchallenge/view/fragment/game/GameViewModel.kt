package app.runchallenge.view.fragment.game

import android.app.Activity
import android.content.Intent
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.game.Game
import app.runchallenge.model.data.game.GameMode
import app.runchallenge.model.data.settings.Setting
import app.runchallenge.controller.repository.game.GameRepositoryImpl
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.model.data.room.RoomError
import app.runchallenge.model.data.room.RoomState
import app.runchallenge.model.extensions.log
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.fragment.base.BaseViewModel
import javax.inject.Inject

class GameViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepositoryImpl,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    override val myToolbarSettings: MyToolbarSettings
        get() = MyToolbarSettings(
            isVisible = false
        )

    /* should be sealed class or multiple live data, */
    data class Data(
        /* has permissions and phone settings */
        val hasSettings: Boolean
    )

    data class GameData(
        /* roomData creation*/
        val intentAndRequestPair: Pair<Intent, Int>,
        /* roomData setup and ready*/
        val roomReady: Boolean
    )

    // settings live data
    val settingLiveData: MyLiveData<Boolean, String> =
        MyLiveData()

    //current game live data
    val gameLiveData: MyLiveData<Game, Game.Error> =
        MyLiveData()

    val roomSetupLiveData: MyLiveData<RoomState, RoomError> =
        MyLiveData()

    lateinit var gameMode: GameMode

    init {
        settingLiveData.addSource(
            settingsRepository,
            { setting: Setting ->

                log("on settings repo : settings ${setting.hasSettings}")
                settingLiveData.setForceSuccess(setting.hasSettings)

                //create/join roomData
                if (setting.hasSettings) {
                    gameRepository.startGame(gameMode)
                }

            },
            { settingError: String? ->
                if (!settingError.isNullOrEmpty()) {
                    settingLiveData.setError(settingError)
                }
                navigateUp()
            })


        /* set roomData network roomConnection listener */

//        roomSetupLiveData.addSource(gameRepository.myRoom.roomStateLiveData, { data ->
//            when (data) {
//                is RoomState.ConnectingStep -> {
//                    log("Room ConnectingStep")
//                    connectingData = data
//                }
//                is RoomState.Connected -> {
//                    log("Room Connected")
//                }
//                is RoomState.Terminated -> {
//                    log("Room Terminated")
//                    navigateUp()
//                }
//            }
//            roomSetupLiveData.setValue { data }
//        }, {
//            log("Room Error")
//            when (it) {
//
//
//                is RoomError.CreateError -> roomSetupLiveData.setError(it)
//
//
//            }
//
//            navigateUp()
//        })
    }

    private var connectingData: RoomState.ConnectingStep? = null


    //TODO Store game when finished and such.
    fun observerGame(gameRoom: MyLiveData<Game, Game.Error>) {
        gameLiveData.addSource(gameRoom, { game ->
            gameLiveData.setValue { game }
        }, { error ->
            gameLiveData.setError(error)
        })
    }


    fun getSettings(activity: Activity) {
        settingsRepository.getSettings(activity)
    }


    override fun onCleared() {
        gameRepository.stopGame()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        connectingData?.let {
            connectingData = null
            it.onActivityResult(requestCode, resultCode, data)
        }
    }


}