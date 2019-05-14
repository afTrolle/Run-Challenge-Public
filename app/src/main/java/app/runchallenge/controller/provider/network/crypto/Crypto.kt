package app.runchallenge.controller.provider.network.crypto

import app.runchallenge.controller.provider.network.message.Message
import java.security.PublicKey
import javax.crypto.spec.SecretKeySpec

interface Crypto {


    /**
     * get public key message to send to participants
     */
    fun getPublicKeyMessage(myParticipantId: String): Message.CryptoMessage.PublicKeyHolder

    /**
     * return participants public key if it is valid
     */
    fun parsePublicKey(
        fromParticipantId: String,
        message: Message.CryptoMessage.PublicKeyHolder
    ): PublicKey?


    /* symmetric key handling */


    /**
     * get shared key message to send to other participants, returns if we have received public key from participant
     */
    fun getSharedKeyMessage(toParticipantId: String): Message.CryptoMessage.SharedKey?

    /**
     * parse received shared key message returns shared key of participant if message is valid
     */
    fun parseSharedKey(
        fromParticipantId: String,
        message: Message.CryptoMessage.SharedKey
    ): SecretKeySpec?

    /**
     * encrypt byte array using shared key
     */
    fun encrypt(bytes: ByteArray): EncryptedContainer

    /**
     * decrypt byte array using shared key from participant
     */
    fun decrypt(fromParticipantId: String, cipheredBytes: ByteArray, iv: ByteArray, mac: ByteArray): ByteArray?



    /* add public key to set */
    fun addPublicKey(senderId: String, key: PublicKey): Crypto

    /* add shared key to set */
    fun addSharedKey(senderId: String, validKey: SecretKeySpec): Crypto

    fun participantIdsOfReceivedSharedKeys(): Set<String>
}