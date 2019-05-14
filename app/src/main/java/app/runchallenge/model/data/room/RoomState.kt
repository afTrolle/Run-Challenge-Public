package app.runchallenge.model.data.room

import android.content.Intent
import androidx.fragment.app.Fragment
import app.runchallenge.model.extensions.startResolutionForResult
import com.google.android.gms.common.api.ResolvableApiException

//RoomState flow   ConnectingStepIntent -> ConnectingStepIntent ->  Connected -> Terminated
// can go to RoomError value at any point also can go from Idle -> Connected -> Terminated
// also  null ->  Terminated
sealed class RoomState(open val roomData: RoomData? = null) {

    // roomData need to do some step which requires view (fragment/activity)
    abstract class ConnectingStep(override val roomData: RoomData?) : RoomState(roomData) {
        abstract fun startActivityForResult(fragment: Fragment)
        abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    // roomData is ready (handshake done)
    data class Connected(override val roomData: RoomData) : RoomState(roomData)

    // left roomData (roomData not active anymore)
    data class Terminated(override val roomData: RoomData?) : RoomState(roomData)

    data class ConnectingStepIntent(
        private val intent: Intent,
        private val requestCode: Int,
        private val activityResultCallback: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit,
        override val roomData: RoomData?
    ) : RoomState.ConnectingStep(roomData) {  // connecting to roomData
        override fun startActivityForResult(fragment: Fragment) {
            fragment.startActivityForResult(intent, requestCode)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            activityResultCallback(requestCode, resultCode, data)
        }
    }

    /**
     * called when an resolvable  expection happens
     * Ie google stuff messed up
     * */
    data class ConnectingStepApiRecover(
        private val resolvableApiException: ResolvableApiException,
        private val requestCode: Int,
        private val activityResultCallback: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit,
        override val roomData: RoomData? = null
    ) : RoomState.ConnectingStep(roomData) {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            activityResultCallback(requestCode, resultCode, data)
        }

        override fun startActivityForResult(fragment: Fragment) {
            resolvableApiException.startResolutionForResult(fragment, requestCode)
        }
    }

}


