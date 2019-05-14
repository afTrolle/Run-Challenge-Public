package app.runchallenge.controller.repository.game

import app.runchallenge.model.data.game.Game
import app.runchallenge.model.data.game.GameError
import app.runchallenge.model.data.game.GameMode
import app.runchallenge.model.extensions.livedata.MyLiveData


interface GameRepository {
    val gameLive: MyLiveData<Game, GameError>

    fun startGame(gameMode: GameMode)

    fun stopGame()
}