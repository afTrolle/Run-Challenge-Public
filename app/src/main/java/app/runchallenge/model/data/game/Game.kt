package app.runchallenge.model.data.game

import app.runchallenge.model.data.auth.AccountError
import app.runchallenge.model.data.room.RoomData


//seperate out data
data class Game(
    val roomData: RoomData? = null
) {
    val isWaitingForReady: Boolean = false

    data class Error(val accountError: AccountError?)


    /**
     * return millis run took
     *
     * */
    fun getFinishedTime(): Long {
        return 0
    }

    /**
     * returns player average run speed in meters/second
     * */
    fun getAverageSpeed(): Float {
        return 0f
    }

    /**
     * retuns players placement,
     * if -1 didn't finish or other issue
     */
    fun getFinishedPlacement(): Int {
        return -1
    }


    fun getPhase(): GamePhases {
        if (roomData == null) {
            return GamePhases.STARTING
        } else {
            return GamePhases.READY
        }


    }

}

enum class GamePhases {
    STARTING,
    READY,
    RUNNING,
    DONE
}

data class GameError(val temp: String) {

}

data class RoomInstance(
    val roomId: String,
    val timestamp: Long,
    val hostID: String,
    val typeOfGame: GameMode,
    val rank: Rank? = null
)


enum class Rank {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}

enum class GameMode {
    RANKED,
    INVITE,
    INBOX,
}