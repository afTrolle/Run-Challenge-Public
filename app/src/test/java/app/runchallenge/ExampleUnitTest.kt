package app.runchallenge


import kotlinx.serialization.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Example local unit NetworkMessage, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun arrow_test() {
        //    val arrow = ArrowExample()

    }

    class MaybeBooleanContainer(val boolean: Boolean)

    @Test
    fun elvisTest() {

        val booleanContainer: MaybeBooleanContainer? = null

        var alwaysboolean: Boolean = false
        if (booleanContainer != null) {
            alwaysboolean = booleanContainer.boolean
        }

        // booleanContainer?.also { alwaysboolean = it.boolean }

        booleanContainer?.let {
            alwaysboolean = it.boolean
        }

        alwaysboolean = booleanContainer?.boolean ?: alwaysboolean


        print(alwaysboolean)
    }


    fun <T : Any> T.updateIfNotNull(newValue: T?): T {
        return newValue ?: this
    }

    @ImplicitReflectionSerializer
    @Test
    fun testSealedClasses() {

//        val msgString = Json.stringify(ReceivedMessage.serializer(), ReceivedMessage(0, Message.Ready(1)))
//        val msgString2 = Json.stringify(ReceivedMessage.serializer(), ReceivedMessage(0, Message.LocationData(2)))
//        println(msgString)
//        println(msgString2)
//        val msg = Json.parse(ReceivedMessage.serializer(), msgString)
//        val msg2 = Json.parse(ReceivedMessage.serializer(), msgString2)
//        println(msg)
//        println(msg2)
//        when (msg2.payload) {
//            is Message.Ready -> assertEquals(1, msg.payload)
//            is Message.LocationData -> assert(false)
//        }
    }

    @Test
    fun testProtoSealedClasses() {

//        val mMessageData: Message.Ready = Message.Ready(1)
//
//
//        val message = mMessageData.toProtoBuff()
//        val decryptedMessage = message.toMessage()
//
//
//        when (decryptedMessage) {
//            is Message.Ready -> assertEquals(mMessageData.bar, decryptedMessage.bar)
//            is Message.LocationData -> assert(false)
//        }
    }
}
