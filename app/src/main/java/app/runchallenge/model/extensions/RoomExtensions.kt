package app.runchallenge.model.extensions

import android.content.Context
import android.content.Intent
import app.runchallenge.model.data.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.*
import com.google.android.gms.games.multiplayer.realtime.Room
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


val Context.signInAccount: GoogleSignInAccount
    get() = GoogleSignIn.getLastSignedInAccount(this)
        ?: throw NotSignedInException()

val Context.playersClient: PlayersClient get() = Games.getPlayersClient(this, this.signInAccount)

/**
 * get multiplayer client if user is signed in or no other ignoreError occurs
 */
val Context.multiPlayerClient: RealTimeMultiplayerClient
    get() = Games.getRealTimeMultiplayerClient(
        this, this.signInAccount
    )

val Context.invitationClient: InvitationsClient get() = Games.getInvitationsClient(this, this.signInAccount)


suspend fun Context.getCurrentPlayer(): Player {
    return playersClient.currentPlayer.toSuspend()
}

suspend fun RealTimeMultiplayerClient.getWaitRoom(room: Room): Intent {
    return getWaitingRoomIntent(room, MIN_OPPONENTS_TO_START_GAME).toSuspend()
}


suspend fun RealTimeMultiplayerClient.getSelectOpponentsIntent(): Intent {
    return getSelectOpponentsIntent(
        MIN_OPPONENTS,
        MAX_OPPONENTS, false
    ).toSuspend()
}

suspend fun InvitationsClient.invitationInboxIntent(): Intent {
    return invitationInboxIntent.toSuspend()
}

suspend fun <T> Task<T>.toSuspend(): T {
    return suspendCoroutine { continuation ->
        addOnSuccessListener { res ->
            continuation.resume(res)
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }
    }
}
