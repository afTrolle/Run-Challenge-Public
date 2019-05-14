package app.runchallenge.controller.provider.network.room.simplfied

import app.runchallenge.model.data.room.RoomConnection
import com.google.android.gms.games.GamesCallbackStatusCodes
import com.google.android.gms.games.multiplayer.realtime.*

/* Wrapper class simplify network calls*/
abstract class SimplifiedRoom {

    //Called when player has joined roomData
    abstract fun onConnectedToRoom(room: Room)

    //Called when all participants are ready in roomData
    abstract fun onAllParticipantsConnectedToRoom(room: Room)

    //Called if this participant left game or got disconnected
    abstract fun onParticipantLeftRoom(disconnected: Boolean)

    //if any opponent left game
    abstract fun onOtherParticipantLeftGame(participantId: String, disconnected: Boolean)

    //failed with some ignoreError
    abstract fun onRoomError(errorMessage: String)

    //when message is received
    abstract fun onMessage(realTimeMessage: RealTimeMessage)

    //current roomData Reference (might change overtime)
    lateinit var room: Room

    /**
     * current value of roomData
     */
    var roomConnection: RoomConnection = RoomConnection.NOT_CONNECTED


    /* private functions */

    /**
     * player connected to a roomData
     */
    private fun onConnected(room: Room) {
        roomConnection = RoomConnection.CONNECTED
        onConnectedToRoom(room)
    }

    /**
     * all participants ready and connected for handshake
     */
    private fun onAllParticipantsConnected(room: Room) {
        onAllParticipantsConnectedToRoom(room)
    }

    private fun leftRoom(disconnected: Boolean = false) {
        roomConnection = if (disconnected) {
            RoomConnection.DISCONNECTED
        } else {
            RoomConnection.LEFT
        }
        onParticipantLeftRoom(disconnected)
    }

    private fun onOtherParticipantLeftGame(disconnected: Boolean = false, participantId: String) {
        onOtherParticipantLeftGame(participantId, disconnected)
    }

    private fun updateRoom(room: Room?) {
        if (room != null) {
            this.room = room
        }
    }

    private fun onError(errorMessage: String) {
        onRoomError(errorMessage)
    }


    /* Callbacks */

    private val realTimeMessageCallback = OnRealTimeMessageReceivedListener {
        onMessage(it)
    }

    private val roomUpdateCallback = object : RoomUpdateCallback() {
        override fun onJoinedRoom(code: Int, room: Room?) {
            updateRoom(room)
            executeIfNoError(code) {
                room?.let { onConnected(it) }
            }
        }

        override fun onLeftRoom(code: Int, room: String) {
            executeIfNoError(code) {
                leftRoom()
            }
        }

        override fun onRoomCreated(code: Int, room: Room?) {
            updateRoom(room)
            executeIfNoError(code) {
                room?.let { onConnected(it) }
            }
        }

        override fun onRoomConnected(code: Int, room: Room?) {
            updateRoom(room)
            executeIfNoError(code) {
                room?.let { onAllParticipantsConnected(it) }
            }
        }

    }

    private val roomStatusUpdateCallback = object : RoomStatusUpdateCallback() {
        override fun onRoomConnecting(room: Room?) {
            updateRoom(room)
        }

        override fun onDisconnectedFromRoom(room: Room?) {
            updateRoom(room)
            leftRoom(true)
        }

        override fun onPeerLeft(room: Room?, participantIds: MutableList<String>) {
            updateRoom(room)
            participantIds.forEach { participantId -> onOtherParticipantLeftGame(false, participantId) }
        }

        override fun onConnectedToRoom(room: Room?) {
            updateRoom(room)
        }

        override fun onPeersDisconnected(room: Room?, participantIds: MutableList<String>) {
            updateRoom(room)
            participantIds.forEach { participantId -> onOtherParticipantLeftGame(true, participantId) }
        }

        override fun onPeerJoined(room: Room?, participantId: MutableList<String>) {
            updateRoom(room)
        }

        override fun onPeersConnected(room: Room?, participantId: MutableList<String>) {
            updateRoom(room)
        }

        override fun onPeerDeclined(room: Room?, participantIds: MutableList<String>) {
            updateRoom(room)
        }

        override fun onPeerInvitedToRoom(room: Room?, participantIds: MutableList<String>) {
            updateRoom(room)
        }

        override fun onP2PDisconnected(participantId: String) {}
        override fun onP2PConnected(participantId: String) {}
        override fun onRoomAutoMatching(room: Room?) {
            updateRoom(room)
        }
    }

    fun getCommonRoomBuilder(): RoomConfig.Builder = RoomConfig.builder(roomUpdateCallback)
        .setOnMessageReceivedListener(realTimeMessageCallback)
        .setRoomStatusUpdateCallback(roomStatusUpdateCallback)

    private fun executeIfNoError(code: Int, function: () -> Unit) {
        if (GamesCallbackStatusCodes.OK == code) {
            function()
        } else {
            val errorMessage = GamesCallbackStatusCodes.getStatusCodeString(code)
            onError(errorMessage)
        }
    }

}