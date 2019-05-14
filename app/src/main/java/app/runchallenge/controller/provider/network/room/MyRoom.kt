package app.runchallenge.controller.provider.network.room

import app.runchallenge.model.data.game.GameMode
import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.model.data.room.RoomError
import app.runchallenge.model.data.room.RoomState
import app.runchallenge.model.extensions.event.EventObservable
import com.google.android.gms.games.multiplayer.Invitation

/**
 * Interface for controlling roomData
 **/
interface MyRoom {

    /* observable of room value */
    val onState: EventObservable<RoomState, RoomError>

    /* observable of room value */
    val onMessage: EventObservable<Message, Nothing>

    /* State functions */
    fun startRoom(gameMode: GameMode)

    suspend fun joinRoom(invitation: Invitation)
    fun leaveRoom()

    suspend fun sendUnReliableMessageToOthers(message: Message): Boolean
    suspend fun sendReliableMessageToOthers(message: Message): Map<String, Boolean>?
}

