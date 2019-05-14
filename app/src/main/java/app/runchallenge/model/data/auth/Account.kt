package app.runchallenge.model.data.auth

import android.net.Uri
import com.google.android.gms.games.Player

sealed class Account {
    /**
     * when user is signed in
     */
    data class UserData(
        val username: String = "",

        val userId: String,

        val userTitle: String? = "Noob",

        val bannerLandscape: Uri? = null,
        val bannerPortrait: Uri? = null,

        val userProfileIcon: Uri? = null,
        val userProfileHighResolution: Uri? = null
    ) : Account()

    /**
     * when user isn't signed in
     */
    object NotSignedIN : Account()
}

val Player.toUserData: Account.UserData
    get() = Account.UserData(
        username = displayName,
        userId = playerId,
        userTitle = title,
        bannerLandscape = bannerImageLandscapeUri,
        bannerPortrait = bannerImagePortraitUri,
        userProfileIcon = iconImageUri,
        userProfileHighResolution = hiResImageUri
    )

//
//fun GoogleSignInAccount.toUserData(context: Context): Account.UserData {
//    val displayName: String = this.displayName.orEmpty()
//    val playerClient: PlayersClient = Games.getPlayersClient(context, this)
//    playerClient.currentPlayer
//        .addOnSuccessListener { player ->
//            player.isMuted
//        }
//        .addOnFailureListener { }
//
//    return Account.UserData(displayName)
//}
//
//val GoogleSignInAccount.toUserData: Account.UserData
//    get() {
//        val displayName: String = this.displayName.orEmpty()
//        val icon =
//            Games.getPlayersClient(this,)
//        return Account.UserData(displayName)
//    }