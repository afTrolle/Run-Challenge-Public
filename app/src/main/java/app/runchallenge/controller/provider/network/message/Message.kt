package app.runchallenge.controller.provider.network.message

import app.runchallenge.controller.provider.network.crypto.Crypto
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import java.lang.Exception

/*
*  TODO when kotlin serialization support byteArray switch for higher optimisations instead of using lists conversions
* */

@Serializable
data class MessageBox(
    //Mandatory
    @SerialId(1) val key: Int,
    //Bytes might be (encrypted) if iv or mac is set
    @SerialId(2) val bytes: List<Byte>,
    //Optional
    @SerialId(3) val iv: List<Byte> = emptyList(),
    @SerialId(4) val mac: List<Byte> = emptyList()
) {
    companion object {
        fun create(bytes: ByteArray): MessageBox {
            return ProtoBuf.load(serializer(), bytes)
        }
    }

    val isEncoded: Boolean
        get() {
            return !(iv.isEmpty() || mac.isEmpty())
        }


    fun toBytes(): ByteArray {
        return ProtoBuf.dump(serializer(), this)
    }
}


sealed class Message {
    @Transient
    var isReliable: Boolean = false
        private set
    @Transient
    var senderId: String = ""
        private set


    internal abstract fun getBytes(): ByteArray
    internal abstract fun getId(): Int
    fun toMessageBox(): MessageBox = MessageBox(getId(), getBytes().toList())

    fun toByteArray(crypto: Crypto): ByteArray {
        return when (this) {
            //don't apply encoding
            is CryptoMessage -> MessageBox(getId(), getBytes().toList())
            else -> {
                val bytes = getBytes()
                val encrypted = crypto.encrypt(bytes)
                MessageBox(getId(), encrypted.message.toList(), encrypted.iv.toList(), encrypted.mac.toList())
            }
        }.toBytes()
    }

    companion object {
        fun create(
            messageBytes: ByteArray,
            isReliable: Boolean,
            senderParticipantId: String,
            crypto: Crypto
        ): Message? {
            val messageBox = MessageBox.create(messageBytes)
            val deCodedBytes = if (messageBox.isEncoded) {
                val encodedBytes = messageBox.bytes.toByteArray()
                val iv = messageBox.iv.toByteArray()
                val mac = messageBox.mac.toByteArray()
                crypto.decrypt(senderParticipantId, encodedBytes, iv, mac)
            } else {
                messageBox.bytes.toByteArray()
            } ?: return null

            val message = toMessage(messageBox.key, deCodedBytes) ?: return null

            message.isReliable = isReliable
            message.senderId = senderParticipantId
            return message
        }

        private fun toMessage(key: Int, bytes: ByteArray): Message? {
            return when (key) {
                CryptoMessage.PublicKeyHolder.messageId -> CryptoMessage.PublicKeyHolder.load(bytes)
                CryptoMessage.SharedKey.messageId -> CryptoMessage.SharedKey.load(bytes)
                Game.GpsState.messageId -> Game.GpsState.load(bytes)
                Game.Location.messageId -> Game.Location.load(bytes)
                Game.Ready.messageId -> Game.Ready.load(bytes)
                else -> null
            }
        }
    }


    sealed class CryptoMessage : Message() {

        @Serializable
        data class PublicKeyHolder(
            @SerialId(10) val key: List<Byte>,
            @SerialId(11) val mac: List<Byte>,
            @SerialId(12) val playerId: List<Byte>
        ) : CryptoMessage() {
            companion object {
                fun load(bytes: ByteArray) = ProtoBuf.load(serializer(), bytes)
                const val messageId = 1
            }

            override fun getBytes(): ByteArray = ProtoBuf.dump(serializer(), this)
            override fun getId(): Int = messageId


            fun playerIdByteArray(): ByteArray = playerId.toByteArray()
            fun macByteArray(): ByteArray = mac.toByteArray()
            fun keyByteArray(): ByteArray = key.toByteArray()
        }

        @Serializable
        data class SharedKey(
            @SerialId(20) val cipheredKey: List<Byte>,
            @SerialId(21) val signature: List<Byte>
        ) : CryptoMessage() {
            companion object {
                fun load(bytes: ByteArray) = ProtoBuf.load(serializer(), bytes)
                const val messageId = 2
            }

            override fun getBytes(): ByteArray = ProtoBuf.dump(serializer(), this)
            override fun getId(): Int = messageId
        }


    }

    /**
     * message sent after crypto handshake
     * Needs CryptoImpl Encoder to get message
     */

    sealed class Game : Message() {

        @Serializable
        data class GpsState(
            @SerialId(399) val isGpsFix: Boolean
        ) : Game() {
            companion object {
                const val messageId = 3
                fun load(bytes: ByteArray) = ProtoBuf.load(serializer(), bytes)
            }

            override fun getBytes(): ByteArray = ProtoBuf.dump(serializer(), this)
            override fun getId(): Int = messageId
        }

        @Serializable
        data class Ready(
            @SerialId(199) val bar: Int
        ) : Game() {
            companion object {
                const val messageId = 4
                fun load(bytes: ByteArray) = ProtoBuf.load(serializer(), bytes)
            }

            override fun getBytes(): ByteArray = ProtoBuf.dump(serializer(), this)
            override fun getId(): Int = messageId
        }

        @Serializable
        data class Location(
            @SerialId(299) val baz: Int
        ) : Game() {
            companion object {
                const val messageId = 5
                fun load(bytes: ByteArray) = ProtoBuf.load(Ready.serializer(), bytes)
            }

            override fun getBytes(): ByteArray = ProtoBuf.dump(serializer(), this)
            override fun getId(): Int = messageId
        }
    }


}

