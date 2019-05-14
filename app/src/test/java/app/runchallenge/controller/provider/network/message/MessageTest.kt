package app.runchallenge.controller.provider.network.message

import app.runchallenge.controller.provider.network.crypto.Crypto
import app.runchallenge.controller.provider.network.crypto.CryptoImpl
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.InvalidClassException
import java.lang.Exception
import java.security.InvalidParameterException
import java.security.PublicKey
import javax.crypto.spec.SecretKeySpec


class MessageTest {

    //test serializer of messages works kinda
    @Test
    fun toByteArray() {

        val senderId = "Hello world"
        val myCrypto = CryptoImpl.create("123456789", "123456789")
        val message = myCrypto.getPublicKeyMessage(senderId)

//        val encryptedMessage = message.toByteArray(myCrypto)
//
//        val decryptedMessage = Message.create(encryptedMessage, false, senderId, myCrypto)

//        assertNotNull(decryptedMessage)
//        assertEquals(decryptedMessage, message)
    }

    @Test
    fun simpleMessage() {

        //do crypto handshake

        val participantOne = "id1"
        val participantTwo = "id2"

        var cryptoOne = CryptoImpl.create("key", "key")
        var cryptoTwo = CryptoImpl.create("key", "key")

        val pKeyOne = cryptoOne.getPublicKeyMessage(participantOne)
        val pKeyTwo = cryptoTwo.getPublicKeyMessage(participantTwo)

        val key = cryptoTwo.parsePublicKey(participantOne, pKeyOne) ?: throw Exception("failed")
        cryptoTwo = cryptoTwo.addPublicKey(participantOne, key) as CryptoImpl

        val keyTwo = cryptoOne.parsePublicKey(participantTwo, pKeyTwo) ?: throw Exception("failed")
        cryptoOne = cryptoOne.addPublicKey(participantTwo, keyTwo) as CryptoImpl

        val sharedKeyOne = cryptoOne.getSharedKeyMessage(participantTwo) ?: throw Exception("failed")
        val sharedKeyTwo = cryptoTwo.getSharedKeyMessage(participantOne) ?: throw Exception("failed")

        cryptoTwo = cryptoTwo.parseSharedKey(participantOne, sharedKeyOne)?.let {
            cryptoTwo.addSharedKey(participantOne, it)
        } as CryptoImpl? ?: throw Exception("failed")

        cryptoOne = cryptoOne.parseSharedKey(participantTwo, sharedKeyTwo)?.let {
            cryptoOne.addSharedKey(participantTwo, it)
        } as CryptoImpl? ?: throw Exception("failed")


        //handshake complete

        val ready = Message.Game.Ready(1)
        val bytes = ProtoBuf.dump(Message.Game.Ready.serializer(), ready)

//        val decodedBytes = cryptoOne.encrypt(bytes).let {
//            cryptoTwo.decrypt(
//                participantOne, it.message.toByteArray(), it.iv.toByteArray(), it.mac.toByteArray()
//            )
//        } ?: throw Exception("failed")

        val encryptedBytes = cryptoOne.encrypt(bytes)

        val decryptedBytes = cryptoTwo.decrypt(
            participantOne,
            encryptedBytes.message,
            encryptedBytes.iv,
            encryptedBytes.mac
        ) ?: fail()

        assertTrue(bytes.contentEquals(decryptedBytes))

        val decryptedMessage = ProtoBuf.load(Message.Game.Ready.serializer(), decryptedBytes)

        assertTrue(decryptedMessage == ready)


        val containrer = MessageBox(
            ready.getId(),
            encryptedBytes.message.toList(),
            encryptedBytes.iv.toList(),
            encryptedBytes.mac.toList()
        )

        val deRcytpedContainer = MessageBox.create(containrer.toBytes())

        assertTrue(containrer == deRcytpedContainer)


        val decryptedMessageBytes = cryptoTwo.decrypt(
            participantOne,
            deRcytpedContainer.bytes.toByteArray(),
            deRcytpedContainer.iv.toByteArray(),
            deRcytpedContainer.mac.toByteArray()
        ) ?: fail()

        assertTrue(bytes.contentEquals(decryptedMessageBytes))

        val data = ProtoBuf.load(Message.Game.Ready.serializer(), decryptedMessageBytes)

        assertEquals(ready, data)

        val messagee = Message.Game.Ready(2)
        val message = messagee.toByteArray(cryptoOne)


        val decoded = Message.create(message, true, participantOne, cryptoTwo) ?: throw Exception("failed")

        assertEquals(messagee, decoded)
    }


}






