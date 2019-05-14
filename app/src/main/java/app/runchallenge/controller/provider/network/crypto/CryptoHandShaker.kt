package app.runchallenge.controller.provider.network.crypto

import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.controller.provider.network.room.MyRoom

class CryptoHandShaker(val myRoom: MyRoom) {

    //Flow of execution


    fun onCryptoMessage(message: Message.CryptoMessage) {
        when (message) {
            is Message.CryptoMessage.PublicKeyHolder -> TODO()
            is Message.CryptoMessage.SharedKey -> TODO()
        }
    }

}