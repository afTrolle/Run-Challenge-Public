package app.runchallenge.controller.provider.network.room

import android.app.Activity
import android.content.Context
import android.content.Intent
import app.runchallenge.R
import app.runchallenge.dagger.ApplicationContextQualifier
import app.runchallenge.model.data.game.GameMode
import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.controller.provider.network.crypto.Crypto
import app.runchallenge.controller.provider.network.crypto.CryptoImpl
import app.runchallenge.controller.provider.network.room.simplfied.SimplifiedRoom
import app.runchallenge.model.data.*
import app.runchallenge.model.data.room.RoomConnection
import app.runchallenge.model.data.room.RoomData
import app.runchallenge.model.data.room.RoomError
import app.runchallenge.model.data.room.RoomState
import app.runchallenge.model.extensions.*
import app.runchallenge.model.extensions.event.EventObservable
import app.runchallenge.model.extensions.state.MyState
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesCallbackStatusCodes
import com.google.android.gms.games.RealTimeMultiplayerClient
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage
import com.google.android.gms.games.multiplayer.realtime.Room
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.ArrayList

//
// Idea start -> connected to room  -> Sends intent -> silent fail ->  Terminated state
// Idea start -> loud error  ->  Terminated state

//Errors none recoverable errors should only be called through roomError
//Other Wise it is just connecting (trying to connect)

//value of roomData connected people
//roomData id, creation date and such

/*
* Flow Idea
*
*  onError -> posts Error -> something deals with ignoreError -> ( if can't deal with it) calls Leave Room -> on left Room Reset value or if not in room reset value
*
* */

data class InternalRoomState(
    val roomData: RoomData? = null,
    val crypto: Crypto? = null,
    val roomConfig: RoomConfig? = null,
    val ignoreError: Boolean = false
)

class MyRoomImpl(
    @ApplicationContextQualifier val context: Context
) : MyRoom, SimplifiedRoom() {


    override val onState = EventObservable<RoomState, RoomError>()
    override val onMessage = EventObservable<Message, Nothing>()

    val state = MyState(InternalRoomState())

    private val myScope = CoroutineScope(Dispatchers.Default)
    private val multiPlayerClient: RealTimeMultiplayerClient = context.multiPlayerClient

    /* Public functions */

    override fun startRoom(gameMode: GameMode) {
        myScope.launch {
            try {
                log("on startRoom gameMode: $gameMode")

                when (gameMode) {
                    GameMode.RANKED -> onRanked()
                    GameMode.INVITE -> onInvite()
                    GameMode.INBOX -> onInbox()
                }

            } catch (e: Exception) {
                onError(e)
            }
        }
    }


    /*
    get create/join room intent
    */

    private suspend fun onInbox() {
        val intent = context.invitationClient.invitationInboxIntent()
        onIntent(intent, RC_INVITATION_INBOX)
    }

    private suspend fun onInvite() {
        val intent = multiPlayerClient.getSelectOpponentsIntent()
        onIntent(intent, RC_SELECT_PLAYERS)
    }

    private suspend fun Room.getWaitRoom(): Intent {
        return multiPlayerClient.getWaitingRoomIntent(this, MIN_OPPONENTS_TO_START_GAME).toSuspend()
    }


    /*
    join or create room logic
    */


    /**
     * create room and join random people
     */
    private suspend fun onRanked() {
        val autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 3, 0)
        val roomConfig = getCommonRoomBuilder().setAutoMatchCriteria(autoMatchCriteria).build()
        setRoomConfig(roomConfig)
        multiPlayerClient.create(roomConfig).toSuspend()
    }

    /**
     * join existing room from invitation
     */
    override suspend fun joinRoom(invitation: Invitation) {
        val roomConfig = getCommonRoomBuilder().setInvitationIdToAccept(invitation.invitationId).build()
        setRoomConfig(roomConfig)
        multiPlayerClient.join(roomConfig).toSuspend()
    }

    /**
     * create room by inviting people
     */
    private suspend fun createRoomInviting(invitees: ArrayList<String>) {
        val roomConfig = getCommonRoomBuilder().addPlayersToInvite(invitees).build()
        setRoomConfig(roomConfig)
        multiPlayerClient.create(roomConfig).toSuspend()
    }


    /*
    leave roomData
    */


    /**
     * try leave room if connected if not connected do cleanup
     * */
    override fun leaveRoom() {
        log("onLeaveRoom called")
        val currentState = state.value

        val config = currentState.roomConfig
        val roomId = currentState.roomData?.Id
        //if connected to a room call leave
        if (roomConnection == RoomConnection.CONNECTED && config != null && roomId != null) {
            multiPlayerClient.leave(config, roomId)
        } else {
            //call cleanup
            cleanUpRoom()
        }
    }

    /**
     * stop worker threads and stop observables
     */
    private fun cleanUpRoom() {
        log("on cleanUpRoom")
        setPublicRoomState(RoomState.Terminated(state.value.roomData))
    }

    /*
    Message handling
    */


    /**
     *  sends message that might not be received by participants
     *
     *  returns true if message left device, false if it failed to try and send
     *  ie not connected to a room or other error to large message
     **/
    override suspend fun sendUnReliableMessageToOthers(message: Message): Boolean {
        val currentState = state.value

        val crypto = currentState.crypto ?: return false
        val roomId = currentState.roomData?.Id ?: return false

        val bytes = message.toByteArray(crypto)

        //can throw exceptions!
        multiPlayerClient.sendUnreliableMessageToOthers(bytes, roomId).toSuspend()
        return true
    }

    /**
     * Sends reliable message to connected participants returns map with result if successful sent message
     */
    override suspend fun sendReliableMessageToOthers(message: Message): Map<String, Boolean>? {
        val currentState = state.value

        val activeParticipantsIds = currentState.roomData?.activeParticipantsIds ?: return null
        val roomId = currentState.roomData.Id
        val crypto = currentState.crypto ?: return null

        val jobs = activeParticipantsIds.map {
            myScope.async { sendReliableMessage(it, message, roomId, crypto) }
        }

        //wait for jobs to complete
        val result = jobs.map {
            try {
                it.await()
            } catch (e: Exception) {
                false
            }
        }

        return (activeParticipantsIds zip result).toMap()
    }

    /**
     * is free from side effects
     * returns true if message was sent
     * */
    private suspend fun sendReliableMessage(
        toParticipant: String,
        message: Message,
        roomId: String,
        crypto: Crypto
    ): Boolean {
        val bytes = message.toByteArray(crypto)

        val boolean: Boolean? = retry(times = 10) {
            try {
                val res = multiPlayerClient.sendReliableSuspend(bytes, roomId, toParticipant)
                if (GamesCallbackStatusCodes.OK == res)
                    return@retry true
            } catch (e: IllegalArgumentException) {

            } catch (e: ApiException) {

            }
            return@retry null
        }

        return boolean ?: false
    }


    /**
     * received message from room
     */
    override fun onMessage(realTimeMessage: RealTimeMessage) {
        myScope.launch {
            val crypto = state.value.crypto ?: return@launch
            when (val message = realTimeMessage.toReceivedMessage(crypto)) {
                null -> return@launch
                is Message.CryptoMessage -> onCryptoMessage(message, message.senderId)
                else -> onMessage.onValue(message)
            }
        }
    }


    /*
    internal callback functions
    */


    /**
     * connected to a room,
     * room might change later on
     */
    override fun onConnectedToRoom(room: Room) {
        myScope.launch {
            try {
                //show wait room
                val waitRoomIntent = room.getWaitRoom()
                onIntent(waitRoomIntent, RC_WAIT_ROOM)
            } catch (exception: Exception) {
                onError(exception, R.string.game_service_failed_to_connect)
            }
        }
    }


    /**
     * when room connection established with all participants ready to start
     **/
    override fun onAllParticipantsConnectedToRoom(room: Room) {
        myScope.launch {
            //create crypto handler
            val crypto = CryptoImpl.create(salt = room.roomId)
            setCrypto(crypto)

            //set room data state
            val roomData = createRoomData(room, context)
            setRoomData(roomData)

            performHandshake()
        }
    }


    /**
     * called when this user left room, might have been because of disconnect
     * can be called multiple times, when room left, first with disconnected and once without
     * */
    override fun onParticipantLeftRoom(disconnected: Boolean) {
        log("onParticipantLeftRoom,  disconnected? $disconnected")
        myScope.launch {
            updateInternalState { oldState ->
                val roomData = oldState.roomData ?: return@updateInternalState oldState
                val currentPlayer = roomData.currentPlayer.onLeft(disconnected)
                oldState.copy(roomData = roomData.copy(currentPlayer = currentPlayer))
            }

            cleanUpRoom()
        }
    }


    /**
     * called when other participant left the game
     */
    override fun onOtherParticipantLeftGame(participantId: String, disconnected: Boolean) {
        log("onOtherParticipantLeftGame participant $participantId , disconnected $disconnected")

        myScope.launch {

            updateInternalState { oldState ->
                val roomData = oldState.roomData ?: return@updateInternalState oldState
                val newRoom = roomData.onParticipantLeft(participantId, disconnected)
                oldState.copy(roomData = newRoom)
            }

        }
    }

    /**
     * some sort of room error occurred,
     * while connecting to room mostly,
     */
    override fun onRoomError(errorMessage: String) {
        log("onMessageError")
        myScope.launch {
            onError(errorMessage = errorMessage)
        }
    }


/* Network Handshake */


/* Utility functions */

    private val onActivityResult: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit =
        { requestCode: Int, resultCode: Int, data: Intent? ->
            log("onActivityResult request : $requestCode, res $resultCode")
            myScope.launch {
                when (requestCode) {
                    RC_SELECT_PLAYERS -> onActivity(resultCode) {

                        val participantsToInvite = data?.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
                            ?: return@onActivity onError()

                        createRoomInviting(participantsToInvite)
                    }
                    RC_INVITATION_INBOX -> onActivity(resultCode) {

                        val invitation = data?.getParcelableExtra<Invitation>(Multiplayer.EXTRA_INVITATION)
                            ?: return@onActivity onError()

                        joinRoom(invitation)
                    }
                    RC_WAIT_ROOM -> onActivity(resultCode)
                    RC_RESOLVE_GAME_ERROR -> onActivity(resultCode, R.string.failed_to_resolve_error)
                }
            }
        }


    private suspend fun onActivity(resultCode: Int, errorRes: Int? = null, resultOk: (suspend () -> Unit)? = null) {
        if (resultCode == Activity.RESULT_OK) {
            resultOk?.invoke()
        } else {
            onError(stringRes = errorRes)
        }
    }


    private fun onIntent(intent: Intent, requestCode: Int) {
        log("onIntent received")
        setPublicRoomState(
            RoomState.ConnectingStepIntent(
                intent,
                requestCode,
                onActivityResult,
                state.value.roomData
            )
        )
    }

    private suspend fun createRoomData(room: Room, context: Context): RoomData {
        val player = context.playersClient.currentPlayer.toSuspend()
        return RoomData.create(room, player)
    }


    /* very naive sending functions */


/*
 * Error handling
 */


    /**
     * Handle any kind of ignoreError
     * */
    private fun onError(
        exception: Exception? = null,
        stringRes: Int? = null,
        errorMessage: String? = null
    ) {
        if (exception is ResolvableApiException) {
            onResolvableException(exception)
        } else if (exception != null || stringRes != null || errorMessage != null) {
            onUnResolvableError(exception, stringRes, errorMessage)
        } else {
            // Something went to shit but not enough to make a fuzz about it
            // just go to terminated state
            leaveRoom()
        }
    }

    /** resolve "ignoreError"
     * might wanna add retry functionality
     * */
    private fun onResolvableException(resolvableApiException: ResolvableApiException) {
        setPublicRoomState(
            RoomState.ConnectingStepApiRecover(
                resolvableApiException,
                RC_RESOLVE_GAME_ERROR,
                onActivityResult
            )
        )
    }

    /**
     * Error that can't be solved
     */
    private fun onUnResolvableError(
        exception: Exception?,
        stringRes: Int?,
        errorMessage: String?
    ) {
        val message = exception?.localizedMessage ?: stringRes?.let { context.getString(it) }
        ?: errorMessage ?: context.getString(R.string.unknown_error)
        onState.onError(RoomError(message))
    }


    /*
        Set mutable state helpers
     */

    private suspend fun setRoomConfig(roomConfig: RoomConfig) {
        updateInternalState { it.copy(roomConfig = roomConfig) }
    }

    private suspend fun setCrypto(crypto: Crypto) {
        updateInternalState { it.copy(crypto = crypto) }
    }

    private suspend fun setRoomData(roomData: RoomData) {
        updateInternalState { it.copy(roomData = roomData) }
    }


    private fun setPublicRoomState(roomState: RoomState) {
        onState.onValue(roomState)
    }

    //this closes the updates


    private suspend fun updateInternalState(s: suspend (InternalRoomState) -> InternalRoomState) {
        state.updateState(s)
    }


    /*





     To be moved
    *
    *
    *
    *
    * */


    private suspend fun onCryptoMessage(message: Message.CryptoMessage, senderId: String) {
        updateInternalState { state ->
            val newCrypto: Crypto? = when (message) {
                is Message.CryptoMessage.PublicKeyHolder -> {
                    //if valid key and we have crypto then add it to crypto and return
                    state.crypto?.parsePublicKey(senderId, message)?.let { validKey ->
                        state.crypto.addPublicKey(senderId, validKey).also {
                            //when public key received send symmetricKey
                            sendSymmetricKey(it, senderId, state)
                        }
                    }
                }
                is Message.CryptoMessage.SharedKey ->
                    //if valid key and we have crypto then add it to crypto and return
                    state.crypto?.parseSharedKey(senderId, message)?.let { validKey ->
                        state.crypto.addSharedKey(senderId, validKey)
                    }
            }
            //update value if we got new crypto other reuse old value
            newCrypto?.let { state.copy(crypto = it) } ?: state
        }
    }

    private fun sendSymmetricKey(crypto: Crypto, senderId: String, state: InternalRoomState) {
        myScope.launch {
            val symmetricKeyMessage = crypto.getSharedKeyMessage(senderId) ?: return@launch
            val roomId = state.roomData?.Id ?: return@launch
            sendReliableMessage(senderId, symmetricKeyMessage, roomId, crypto)
        }
    }


/*
*
*  Security Handshake
*
**/


    // 1. Connected To Room ->
    // 2. (all) Send public key to Other participants ->
    // 3. (receiver of public key) respond to public key with encrypt shared key with given public key
    // 4. wait until received all shared keys from connected participants, if not received shared key after x millis
    // re-send public key again, sending key might have failed.

    /* apperently google send reliable message isn't 100% reliable
    * You get flaky results if you don't leave a room correctly
    * by sending message to a new room but haven't left old,
    * happens often in emulator when it kills app to fast to leave. ie on destroy isn't called to app
    *
    * So to start we need some sort of handshake which is if we haven't
    * Received symmetric key from all active participants we can assume our public key wasn't received
    * so we have to try and re send it.
    **/
    private suspend fun performHandshake() {
        try {
            //try send public message
            val result = withTimeoutOrNull(15000L) {
                val running = true
                while (running) {
                    log("trying to send data or something")
                    val currentState = state.value
                    val crypto = currentState.crypto
                    val room = currentState.roomData

                    if (crypto != null && room != null) {
                        val activeParticipants = room.activeParticipants.map { it.id }
                        val participantsReceivedSymmetricKey = crypto.participantIdsOfReceivedSharedKeys()
                        val remainingToReceiveSymmetricKey = activeParticipants.minus(participantsReceivedSymmetricKey)

                        if (remainingToReceiveSymmetricKey.isEmpty()) {
                            return@withTimeoutOrNull true
                        } else {
                            sendPublicKey(crypto, room, remainingToReceiveSymmetricKey)
                        }
                    }
                    delay(500)
                }
                return@withTimeoutOrNull false
            } ?: false



            if (!result) {
                //failed handshake
                onError(stringRes = R.string.encryption_hand_shake_falied)
                leaveRoom()
            } else {
                //Ready to start game
                val roomData = state.value.roomData ?: return onError(stringRes = R.string.room_data_missing)
                setPublicRoomState(RoomState.Connected(roomData))
            }
        } catch (e: Exception) {

        }
    }

    private suspend fun sendPublicKey(crypto: Crypto, room: RoomData, toParticipant: List<String>) {
        val publicKey = crypto.getPublicKeyMessage(room.currentPlayer.id)

        val sending = toParticipant.map { participant ->
            //soo cool!
            myScope.async {
                sendReliableMessage(participant, publicKey, room.Id, crypto)
            }
        }

        return sending.joinAll()
    }

}

