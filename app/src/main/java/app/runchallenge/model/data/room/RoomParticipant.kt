package app.runchallenge.model.data.room

import android.net.Uri
import android.os.SystemClock
import com.google.android.gms.games.Player

data class RoomParticipant(
    val id: String,
    val playerName: String,
    val leftTimestamp: Long? = null,
    val leftByDisconnect: Boolean? = null,
    val player: Player? = null,
    val iconImageUri: Uri?,
    val highResProfile: Uri?
) {

    fun onLeft(isDisconnected: Boolean = false): RoomParticipant {

        val elapsed = SystemClock.elapsedRealtime()
        //update timestamp only if  timestamp has not been set
        return if (leftTimestamp == null) {
            copy(leftTimestamp = elapsed, leftByDisconnect = isDisconnected)
        } else {
            copy(leftByDisconnect = isDisconnected)
        }
    }

    val isConnected = (leftTimestamp == null)

}