package app.runchallenge.controller.provider.network.crypto


data class EncryptedContainer(
    val message: ByteArray,
    val iv: ByteArray,
    val mac: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedContainer

        if (!message.contentEquals(other.message)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!mac.contentEquals(other.mac)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + mac.contentHashCode()
        return result
    }
}


