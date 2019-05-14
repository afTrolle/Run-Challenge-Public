package app.runchallenge.model.data.room

import app.runchallenge.model.data.game.League
import com.google.android.gms.games.Player
import com.google.android.gms.games.multiplayer.Participant
import com.google.android.gms.games.multiplayer.realtime.Room

data class RoomData(
    val Id: String,
    val creationTimeStamp: Long,
    val league: League,
    val currentPlayer: RoomParticipant,
    val otherPlayers: List<RoomParticipant>
) {

    companion object {
        fun create(room: Room, player: Player, league: League = League.UNKNOWN): RoomData {

            val userParticipantId = room.getParticipantId(player.playerId)

            val userParticipant = room.getParticipant(userParticipantId)

            val otherParticipant = room.participants.minus(userParticipant).map { it.toRoomParticipant() }

            return RoomData(
                room.roomId,
                room.creationTimestamp,
                league,
                userParticipant.toRoomParticipant(),
                otherParticipant
            )
        }
    }


    fun onParticipantLeft(participantId: String, byDisconnect: Boolean): RoomData = otherPlayers.map {
        if (it.id.contentEquals(participantId))
            it.onLeft(byDisconnect)
        else
            it
    }.let { copy(otherPlayers = it) }


    val activeParticipants by lazy { otherPlayers.filter { it.isConnected } }
    val activeParticipantsIds by lazy { activeParticipants.map { it.id } }
}

fun Participant.toRoomParticipant(): RoomParticipant {
    return RoomParticipant(
        id = this.participantId,
        playerName = this.displayName,
        highResProfile = this.hiResImageUri,
        iconImageUri = this.iconImageUri,
        player = this.player
    )
}