package app.runchallenge.model.extensions

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import app.runchallenge.BuildConfig
import app.runchallenge.R
import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.controller.provider.network.crypto.Crypto
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.games.RealTimeMultiplayerClient
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun Set<Location>.sortLocations(): SortedSet<Location> {
    return this.toSortedSet(kotlin.Comparator { o1, o2 -> o1.elapsedRealtimeNanos.compareTo(o2.elapsedRealtimeNanos) })
}


fun Fragment.alert(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun View.showError(string: String?) {
    if (string == null)
        return
    Snackbar.make(this, string, Snackbar.LENGTH_SHORT).show()
}

fun View.showHint(string: String) {
    val color = context.getColor(R.color.onAccent)
    val snackbar = Snackbar.make(this, string, Snackbar.LENGTH_SHORT).setTextColor(color).show()
}

fun Snackbar.setTextColor(@ColorInt color: Int): Snackbar {
    val tv = view.findViewById(R.id.snackbar_text) as TextView
    tv.setTextColor(color)
    return this
}


fun getTrophyColor(placement: Int): Int =
    when (placement) {
        1 -> R.color.gold
        2 -> R.color.silver
        3 -> R.color.bronze
        4 -> R.color.iron
        else -> R.color.grey600
    }

fun Any.log(message: String) {
    if (BuildConfig.DEBUG)
        Log.d(this.javaClass.simpleName, message)
}


fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED
}


fun ResolvableApiException.startResolutionForResult(fragment: Fragment, requestCode: Int) {
    val intentSender = resolution?.intentSender
    if (intentSender != null) {
        fragment.startIntentSenderForResult(intentSender, requestCode, null, 0, 0, 0, null)
    }
}

fun RealTimeMessage.toReceivedMessage(crypto: Crypto): Message? {
    return Message.create(messageData, isReliable, senderParticipantId, crypto)
}


suspend fun RealTimeMultiplayerClient.sendReliableSuspend(
    bytes: ByteArray,
    roomId: String,
    toParticipant: String
): Int {
    return suspendCoroutine { continuation ->
        this.sendReliableMessage(bytes, roomId, toParticipant) { statusCode, _, _ ->
            continuation.resume(statusCode)
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }
    }
}

/**
 * Retry with exponential backoff
 * breaks retry on exceptions and if block returns value other than null
 */
suspend fun <T> retry(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T?
): T? {
    var currentDelay = initialDelay
    repeat(times - 1) {

        val result = block()
        if (result != null)
            return result

        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}


fun <T : Any> Mutex.runBlockingWith(action: () -> T) {
    runBlocking {
        withLock(null, action)
    }
}
