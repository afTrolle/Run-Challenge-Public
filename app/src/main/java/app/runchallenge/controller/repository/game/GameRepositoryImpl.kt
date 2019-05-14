package app.runchallenge.controller.repository.game

import android.content.Context
import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.model.data.game.GameMode
import app.runchallenge.controller.provider.network.room.MyRoom
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.controller.repository.game.active.GameEffects
import app.runchallenge.controller.repository.game.active.GameStates
import app.runchallenge.controller.repository.game.active.ReadyState
import app.runchallenge.controller.service.location.LocationService
import app.runchallenge.model.data.game.Game
import app.runchallenge.model.data.game.GameError
import app.runchallenge.model.data.location.LocationData
import app.runchallenge.model.data.location.LocationError
import app.runchallenge.model.data.room.RoomError
import app.runchallenge.model.data.room.RoomState
import app.runchallenge.model.extensions.livedata.MyLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameRepositoryImpl(
    val settingsRepository: SettingsRepository,
    val userRepository: UserRepository,
    private val myRoom: MyRoom,
    private val context: Context
) : GameRepository, GameEffects {

    private var gameState: GameStates = ReadyState(Game(), this)
    private val mutex = Mutex()
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override val gameLive: MyLiveData<Game, GameError> = MyLiveData()


    /*
    *
    * repo functions
    *
    **/

    override fun startGame(gameMode: GameMode) {
        if (startLocationService(context)) {
            myRoom.startRoom(gameMode)
            startListenToRoom()
        } else {
            //failed to start gps
            onStartError()
        }
    }

    /* shut down game */
    override fun stopGame() {
        myRoom.leaveRoom()
        stopLocationService()
        stopListenToRoom()
    }


    /* Location Service */

    private var locationBinding: LocationService? = null
    //  private var subscribtion: Disposable? = null


    private val onLocation: (locationData: LocationData) -> Unit = { locationData ->
        updateGameState {
            it.onLocation(locationData)
        }
    }


    private val onLocationError: (locationError: LocationError?) -> Unit = { error ->
        updateGameState {
            it.onLocationError(error)
        }
    }


    private fun startLocationService(context: Context): Boolean = LocationService.startService(context) {
        locationBinding = it
        //connected to service
        // it?.locationProviderImpl?.subscribeByKey(this, onLocation, onLocationError)
//        subscribtion = it?.locationProviderImpl?.subject?.subscribeBy(onError = {
//
//        }, onValue = {
//
//        })
    }

    private fun stopLocationService() {
        //   locationBinding?.locationProviderImpl?.unSubscribeByKey(this)
//        if (locationBinding != null && false == subscribtion?.isSubscribed) {
//            //  subscribtion?.unSubscribe()
//        }
        LocationService.stopService(context)
    }


    /*
    *
    *room updates
    *
    * */

    private val onMessage = { message: Message ->
        updateGameState {
            it.onMessage(message)
        }
    }


    private val onRoomUpdate = { room: RoomState ->
        updateGameState {
            it.onRoomUpdate(room)
        }
    }

    private val onRoomError = { error: RoomError? ->
        updateGameState {
            it.onRoomError(error)
        }
    }

    private fun startListenToRoom() {
        //myRoom.onMessage = onMessage
        //myRoom.onRoomUpdate = onRoomUpdate
        //myRoom.onRoomError = onRoomError
    }

    private fun stopListenToRoom() {
        //myRoom.onMessage = null
        //myRoom.onRoomUpdate = null
        //myRoom.onRoomError = null
    }


    /* State Side Effects */


    override fun reliableMessageToOthers() {

    }

    override fun unReliableMessageToOthers() {

    }


    /*
    *
    * Internal functions
    *
    **/

    private fun onStartError() {

    }


    private fun updateGameState(getNewState: (gameStates: GameStates) -> GameStates) {
        scope.launch {
            mutex.withLock {
                val newGameStates = getNewState(gameState)
                if (gameState != newGameStates) {
                    //data has changed
                    gameLive.setValue { newGameStates.gameData }
                    gameState = newGameStates
                }
            }
        }
    }

}

