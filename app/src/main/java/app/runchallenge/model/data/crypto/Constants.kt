package app.runchallenge.model.data.crypto

//hashing used with pre shared key to check integrity
const val MAC_ALGORITHM = "HmacSHA512"
const val HASH_ALGORITHM = "SHA-512"

// symmetric key used to send data to connected participants
const val MY_SHARED_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
const val MY_SHARED_ALGORITHM = "AES"
const val MY_SHARED_KEY_LENGTH = 256

//used to exchange symmetric keys
const val RSA_SIGNING_ALGORITHM = "SHA512withRSA"
const val RSA_CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"
const val RSA_KEY_ALGORITHM = "RSA"
const val RSA_KEY_LENGTH: Int = 2048
