package app.runchallenge.controller.repository.game.active

import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.model.data.game.Game
import app.runchallenge.model.data.location.LocationData
import app.runchallenge.model.data.location.LocationError
import app.runchallenge.model.data.room.RoomError
import app.runchallenge.model.data.room.RoomState


//side effects from states
interface GameEffects {
    //sends reliable message to others people connected to room, get ack on message.
    //however noticed we might get fake ack
    fun reliableMessageToOthers()

    //sends unReliableMessage to other people connected
    fun unReliableMessageToOthers()

}

// State Super class kinda,
sealed class GameStates(
    open var gameData: Game,
    protected open val effects: GameEffects
) {

    /* State Events to receive  */

    open fun onLocation(locationData: LocationData): GameStates = this
    open fun onLocationError(error: LocationError?): GameStates = this


    open fun onMessageError(message: RoomError?): GameStates = this
    open fun onRoomUpdate(room: RoomState): GameStates = this
    open fun onRoomError(error: RoomError?): GameStates = this
    open fun onMessage(message: Message): GameStates = this

    //not sure about these
    open fun onCountDownStarted(): GameStates = this

    open fun onStop(): GameStates = this

}

/**
 * Ready value is doing a ready check for all players wait until all players sent ready message to each other
 *
 */
data class ReadyState(override var gameData: Game, override val effects: GameEffects) :
    GameStates(gameData, effects) {

    override fun onMessage(message: Message): GameStates {
        val openMessage = message
        when (openMessage) {
            is Message.Game.Ready -> {

            }
            is Message.Game.Location -> TODO()
        }


        return this
    }

    override fun onLocation(locationData: LocationData): GameStates {

        //val isGpsFixed = locationData.isGpsFixed()

        //  gameData.

        //  effects.reliableMessageToOthers()


        return super.onLocation(locationData)
    }


}


/**
 * Running value receive location update value (calculate distance run)
 * (send update of distance that has been run)
 *
 **/
data class RunningState(override var gameData: Game, override val effects: GameEffects) :
    GameStates(gameData, effects) {


}


/**
 * Stop sending location updates but keep recvieing until all finished or people left room.
 * */
data class FinishedState(override var gameData: Game, override val effects: GameEffects) :
    GameStates(gameData, effects) {


}


