package app.runchallenge.view.fragment.game

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import app.runchallenge.R
import app.runchallenge.model.extensions.log
import app.runchallenge.view.activity.ActivityViewModel
import app.runchallenge.view.fragment.base.BaseFragment
import app.runchallenge.model.extensions.showError
import app.runchallenge.model.data.game.Game
import app.runchallenge.model.data.game.GameMode
import app.runchallenge.model.data.room.RoomState
import kotlinx.android.synthetic.main.fragment_game.*

private const val ARG_PARAM1 = "param1"

private const val MODE_RANKED = 1 // random opponents
private const val MODE_INVITE = 2 //invite friends to play against you
private const val MODE_INBOX = 3 // inbox friends inviting you to play


class GameFragment : BaseFragment() {
    override val viewModelClass = GameViewModel::class.java

    override val layout: Int = R.layout.fragment_game

    lateinit var myViewModel: GameViewModel

    lateinit var activityViewModel: ActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myViewModel = viewModel as GameViewModel
        activityViewModel = ViewModelProviders.of(this.activity!!, viewModeFactory).get(ActivityViewModel::class.java)

        myViewModel.gameMode = getGameMode()

        myViewModel.settingLiveData.observer(this,
            { hasSettings ->
                if (!hasSettings) {
                    log("start Get settings")
                    activity?.also {
                        myViewModel.getSettings(it)
                    }
                }
            }, { error ->
                error?.let { view?.showError(it) }
            })


        //observer
        myViewModel.roomSetupLiveData.observer(this, { data ->
            when (data) {
                is RoomState.ConnectingStep -> data.startActivityForResult(this)
                is RoomState.Connected -> {
                }
                is RoomState.Terminated -> {
                }
            }
        }, { error ->
            //            when (ignoreError) {
//                is RoomMaintainer.Error.Resolvable -> ignoreError.startResolution(activity)
//                is RoomMaintainer.Error.UnResolvable -> view?.showError(ignoreError.message)
//            }
        })

        //TODO FIX ME
//        myViewModel.gameLiveData.observer(this, {
//            val roomSetupData = it.roomMaintainerData
//            when (roomSetupData) {
//                is RoomMaintainer.Data.Connected -> {
//                    //TODO enableChannel game data
//                }
//                is RoomMaintainer.Data.SetupStep -> roomSetupData.startActivityForResult(this)
//            }
//        }, {
//            val roomSetupError = it?.roomMaintainerError
//            when (roomSetupError) {
//                is RoomMaintainer.Error.Resolvable -> activity?.let { activity -> roomSetupError.startResolution(activity) }
//                is RoomMaintainer.Error.UnResolvable -> {
//                    roomSetupError.message?.let { it1 -> view?.showError(it1) }
//                }
//            }
//
//        })

    }


    fun setView(game: Game?): Unit {

        if (game == null) {
            //enableChannel spinning loading thingy


        } else if (game.isWaitingForReady) {
            //enableChannel
            game_ready_view.visibility = View.VISIBLE
            game_LoadingProgress.visibility = View.GONE
        }

    }


    private fun getGameMode(): GameMode {
        val arguments = arguments?.getInt(ARG_PARAM1)
        return when (arguments) {
            MODE_INBOX -> GameMode.INBOX
            MODE_INVITE -> GameMode.INVITE
            MODE_RANKED -> GameMode.RANKED
            else -> throw NotImplementedError()
        }
    }

    override fun onPause() {
        activityViewModel.setDoubleBackButton(false)
        super.onPause()
    }

    override fun onResume() {
        activityViewModel.setDoubleBackButton(true)
        super.onResume()
    }


}