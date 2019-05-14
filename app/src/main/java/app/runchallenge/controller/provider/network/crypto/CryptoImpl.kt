package app.runchallenge.controller.provider.network.crypto

import app.runchallenge.BuildConfig
import app.runchallenge.controller.provider.network.message.Message
import app.runchallenge.model.data.crypto.*
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/* Observer all function calls are clean. Ie Should have no side effects */
data class CryptoImpl(
    private val preSharedKey: SecretKey,
    private val symmetricKey: SecretKey,
    private val publicKey: PublicKey,
    private val privateKey: PrivateKey,
    private val othersPublicKeys: Map<String, PublicKey> = mapOf(),
    private val othersSharedKeys: Map<String, SecretKey> = mapOf()
) : Crypto {


    /* public key handling */

    override fun addSharedKey(senderId: String, validKey: SecretKeySpec): Crypto {
        val newOtherPublicKeys = othersSharedKeys.plus(senderId to validKey)
        return copy(othersSharedKeys = newOtherPublicKeys)
    }

    override fun addPublicKey(senderId: String, key: PublicKey): Crypto {
        val newOtherPublicKeys = othersPublicKeys.plus(senderId to key)
        return copy(othersPublicKeys = newOtherPublicKeys)
    }

    override fun participantIdsOfReceivedSharedKeys(): Set<String> {
        return othersSharedKeys.keys
    }

    companion object {
        fun create(preSharedSecret: String = BuildConfig.GoogleSecAPIKEY, salt: String): CryptoImpl {

            val preSharedBytes = preSharedSecret.toByteArray(Charsets.UTF_8)
            val saltBytes = filterSalt(salt).toByteArray(Charsets.UTF_8)

            //mac key, check for integrity
            val preSharedKey = getMacKeyUsingHashing(listOf(preSharedBytes, saltBytes))

            // init key to be shared
            // I.e key used to decrypt all data-messages from client
            val mySharedKey = generateKey(
                MY_SHARED_ALGORITHM,
                MY_SHARED_KEY_LENGTH
            )

            //Generate keys used for fingerprinting and integrity, shared key exchange
            val keyPair = generateKeyPair(
                RSA_KEY_ALGORITHM,
                RSA_KEY_LENGTH
            )

            return CryptoImpl(
                preSharedKey = preSharedKey,
                symmetricKey = mySharedKey,
                publicKey = keyPair.public,
                privateKey = keyPair.private
            )
        }
    }


    override fun getPublicKeyMessage(myParticipantId: String): Message.CryptoMessage.PublicKeyHolder {
        val publicEncodedKey = publicKey.encoded
        val participantIdBytes = myParticipantId.toByteArray(Charsets.UTF_8)

        //ORDER IS important!
        val mac = macData(listOf(participantIdBytes, publicEncodedKey))

        return Message.CryptoMessage.PublicKeyHolder(
            publicEncodedKey.toList(),
            mac.toList(),
            participantIdBytes.toList()
        )
    }

    override fun parsePublicKey(
        fromParticipantId: String,
        message: Message.CryptoMessage.PublicKeyHolder
    ): PublicKey? {
        //ORDER IS important!
        val macData = listOf(message.playerIdByteArray(), message.keyByteArray())
        val mac = macData(macData)

        // check that mac is correct
        if (mac.contentEquals(message.macByteArray())) {
            val fromParticipantIdBytes = fromParticipantId.toByteArray(Charsets.UTF_8)
            //if mac is correct, check that it is the same participant id as from google api
            if (fromParticipantIdBytes.contentEquals(message.playerIdByteArray())) {
                return generatePublicKey(message.keyByteArray())
            }
        }
        return null
    }


    override fun getSharedKeyMessage(toParticipantId: String): Message.CryptoMessage.SharedKey? {
        //participants public key (receiving my shared key)
        val toPublicKey = othersPublicKeys[toParticipantId] ?: return null
        //my shared key
        val mySharedKey = symmetricKey.encoded

        //encrypted my shared key
        val encryptedMySharedKey = cipherWithRSA(toPublicKey, mySharedKey)
        //signed with my private so needs public key from me to check signature
        val signature = signWithRSA(listOf(encryptedMySharedKey))

        //maybe add sender id here,
        return Message.CryptoMessage.SharedKey(
            encryptedMySharedKey.toList(),
            signature.toList()
        )
    }

    /*
    * return shared key if valid
    * */
    override fun parseSharedKey(
        fromParticipantId: String,
        message: Message.CryptoMessage.SharedKey
    ): SecretKeySpec? {
        val fromPublicKey = othersPublicKeys[fromParticipantId] ?: return null
        val valid =
            validateWithRSA(fromPublicKey, listOf(message.cipheredKey.toByteArray()), message.signature.toByteArray())
        if (valid) {
            val rawSharedKey = deCipherWithRsa(privateKey, message.cipheredKey.toByteArray())
            return SecretKeySpec(rawSharedKey, MY_SHARED_ALGORITHM)
        }
        return null
    }

    override fun encrypt(bytes: ByteArray): EncryptedContainer {
        val cipher = cipherWithSymmetric()
        val cipheredMessage = cipher.doFinal(bytes)
        val iv = cipher.iv
        val mac = signWithRSA(listOf(cipheredMessage, iv))
        return EncryptedContainer(cipheredMessage, iv, mac)
    }


    override fun decrypt(
        fromParticipantId: String,
        cipheredBytes: ByteArray,
        iv: ByteArray,
        mac: ByteArray
    ): ByteArray? {
        val fromPublicKey = othersPublicKeys[fromParticipantId] ?: return null
        val fromSharedKey = othersSharedKeys[fromParticipantId] ?: return null

        if (validateWithRSA(fromPublicKey, listOf(cipheredBytes, iv), mac)) {
            return deCipherWithSymmetric(fromSharedKey, iv, cipheredBytes)
        }
        return null
    }


    /* Helper functions */


    private val mac: Mac
        get() = Mac.getInstance(MAC_ALGORITHM)
            .also { it.init(preSharedKey) }


    private fun cipherWithSymmetric(): Cipher = Cipher.getInstance(MY_SHARED_CIPHER_ALGORITHM).also {
        it.init(Cipher.ENCRYPT_MODE, symmetricKey)
    }


    private fun signWithRSA(data: List<ByteArray>): ByteArray {
        val s = Signature.getInstance(RSA_SIGNING_ALGORITHM)
        s.initSign(privateKey)
        data.forEach {
            s.update(it)
        }
        return s.sign()
    }


    private fun macData(data: List<ByteArray>): ByteArray {
        val mac = this.mac
        data.forEach {
            mac.update(it)
        }
        return mac.doFinal()
    }


    private fun deCipherWithSymmetric(sharedKey: SecretKey, iv: ByteArray, data: ByteArray): ByteArray {
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(MY_SHARED_CIPHER_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, sharedKey, ivSpec)
        return cipher.doFinal(data)
    }


    private fun generatePublicKey(keyByteArray: ByteArray): PublicKey {
        val pubKeySpec = X509EncodedKeySpec(keyByteArray)
        return KeyFactory.getInstance(RSA_KEY_ALGORITHM).generatePublic(pubKeySpec)
    }


    private fun cipherWithRSA(publicKey: PublicKey, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            publicKey
        )
        return cipher.doFinal(data)
    }

    private fun deCipherWithRsa(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_CIPHER_ALGORITHM)
        cipher.init(
            Cipher.DECRYPT_MODE,
            privateKey
        )
        return cipher.doFinal(data)
    }

    private fun validateWithRSA(publicKey: PublicKey, data: List<ByteArray>, signature: ByteArray): Boolean {
        val s = Signature.getInstance(RSA_SIGNING_ALGORITHM)
        s.initVerify(publicKey)
        data.forEach {
            s.update(it)
        }
        return s.verify(signature)
    }

}

private fun getMacKeyUsingHashing(bytes: List<ByteArray>): SecretKeySpec {
    val digester = MessageDigest.getInstance(HASH_ALGORITHM)
    bytes.forEach {
        digester.update(it)
    }
    val saltedPreSharedKey = digester.digest()

    return SecretKeySpec(saltedPreSharedKey, MAC_ALGORITHM)
}

private fun generateKey(algorithm: String, keyLength: Int): SecretKey {
    val instance = KeyGenerator.getInstance(algorithm)
    instance.init(keyLength)
    return instance.generateKey()
}

private fun generateKeyPair(algorithm: String, keyLength: Int): KeyPair {
    val instance = KeyPairGenerator.getInstance(algorithm)
    instance.initialize(keyLength)
    return instance.genKeyPair()
}


private fun filterSalt(rawSalt: String): String {
    val regex = "^[A-Za-z]+".toRegex()
    return regex.find(rawSalt)?.value ?: "Missing Salt"
}
