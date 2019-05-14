package app.runchallenge.examples


//open allow for over-writting
// constructor keyword used to indicate that this is a constructor (using private or other modifier)
open class Base private constructor(val x: Int, val extra: String) {

    //Secondary Constructors
    //need to fulfill primary constructor :this()
    //you can use default values if not defined
    constructor(x: Int) : this(x, "none") {
        print("secondary constructor running")
    }

    //open means that function can be overridden
    open fun f() {
        print("base f")
    }

    //can't be overwritten
    fun a() {
        print("a")
    }

}

interface Interface {
    fun f() {
        print("interface f")
    }
}


//if you inherit from multiple classes with function name you have to override it
class Derviedclass(x: Int, val storedInObject: Int) : Base(x), Interface {

    override fun f() {
        super<Interface>.f()
        super<Base>.f()
    }


}


//abstract class AbstractClass : {
//    //override abstract fun f()
//}