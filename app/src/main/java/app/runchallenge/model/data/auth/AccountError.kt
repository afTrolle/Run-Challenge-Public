package app.runchallenge.model.data.auth

import android.app.Activity
import app.runchallenge.controller.provider.RC_RESOLVE_ERROR
import com.google.android.gms.common.api.Status

sealed class AccountError {
    /**
     * Error that can be resolved
     */
    data class ResolvableError(private val resolvableErrorStatus: Status) : AccountError() {
        fun startResolution(activity: Activity) {
            resolvableErrorStatus.startResolutionForResult(activity,
                RC_RESOLVE_ERROR
            )
        }
    }

    /**
     * Error that can't be resolved
     */
    data class UnResolvableError(val message: String) : AccountError()
}