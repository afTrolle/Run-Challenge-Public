package app.runchallenge.view.fragment.home


import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import app.runchallenge.R
import app.runchallenge.model.data.game.Game
import app.runchallenge.model.extensions.log
import app.runchallenge.view.fragment.base.BaseFragment
import com.google.android.gms.common.images.ImageManager
import kotlinx.android.synthetic.main.fragment_home.*
import app.runchallenge.model.extensions.getTrophyColor
import app.runchallenge.model.extensions.showHint
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : BaseFragment() {

    override val layout: Int = R.layout.fragment_home

    override val viewModelClass = HomeViewModel::class.java

    private lateinit var myViewModel: HomeViewModel

    private lateinit var imageManager: ImageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myViewModel = viewModel as HomeViewModel

        imageManager = ImageManager.create(context)
        myViewModel.onCreate()

        myViewModel.liveData.observer(this, {
            updateUi(it)
        }, {
            //TODO print ignoreError

        })
    }

    fun updateUi(data: HomeViewModel.HomeData) {
        log("received user data")
        home_user_name.text = data.userData.username
        imageManager.loadImage(
            home_user_profile_image_view,
            data.userData.userProfileHighResolution,
            R.drawable.ic_account
        )

        updateUiPreviousGame(data.previousGame)
    }

    fun updateUiPreviousGame(previousGame: Game) {

        val placement = previousGame.getFinishedPlacement()

        val trophyColor = getTrophyColor(placement)
        context?.let {
            val color = ContextCompat.getColor(it, trophyColor)
            home_previous_trophy.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        val trophyText = getTrophyText(placement)

        home_previous_placement_text.text = getString(trophyText)

        home_previous_finished_time.text = getFinishedTimeString(previousGame.getFinishedTime())

        home_previous_average_speed.text = previousGame.getAverageSpeed().toString()
    }

    private fun getFinishedTimeString(time: Long): String {
        val date = Date(time)
        val formatter = SimpleDateFormat("H:mm:ss:SS", Locale.ROOT)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    private fun getTrophyText(placement: Int?): Int {
        return when (placement) {
            1 -> R.string.placment_first
            2 -> R.string.placement_second
            3 -> R.string.placment_third
            4 -> R.string.placment_fourth
            else -> R.string.placment_unknown
        }
    }

    override fun onResume() {
        super.onResume()
        myViewModel.liveData.success?.let {
            updateUi(it)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* register on click listeners */
        home_button_challenge.setOnClickListener {
            myViewModel.onChallengeClicked()
        }

        home_button_challenge_friend.setOnClickListener {
            myViewModel.onChallengeFriendClicked()
        }

        home_button_history.setOnClickListener {
            myViewModel.onHistoryClicked()
        }

        home_button_inbox.setOnClickListener {
            myViewModel.onInviteInbox()
        }

        home_setting_button.setOnClickListener {
            myViewModel.onSettingClicked()
        }

        home_previous_layout.setOnClickListener {
            val message = getString(R.string.home_previous_game_info_hint)
            it.showHint(message)
        }
    }

}
