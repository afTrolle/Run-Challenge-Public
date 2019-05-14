package app.runchallenge.examples

import kotlin.random.Random

fun main() {
    print("hello world")


    //Variables

    //Types is figured out at compile/before compile

    //Val (Immutable vars)
    val name: String = "alex"

    //Var mutable vars (avoid as much as possible)
    var myAge: Int = 27

    //string interpolation add to string inside string.
    println("My name is $name and My age is $myAge")

    //Code exectuion return is put in string
    println("1 +1 is ${1 + 1}")

    //Common DataTypes
    // Double Float Boolean Short Byte Long

    //Type checking
    val typeChecking = (name is String)

    //Casting
    val floatNum: Float = 1.23F
    val floatToInt = floatNum.toInt()


    val charList = "Alexander \n"

    //A raw string is delimited by a triple quote ("""), contains no escaping and can contain newlines and any other characters:
    val longStr = """this is a multiline
        \n is included in the string
        string"""

    //Boolean
    if (true is Boolean) {
        println("is boolean")
    }

    //Comparing
    val string1 = "A random String"
    val string2 = "a random String"
    var string3 = "A random String"

    println("String Equal: ${string1.equals(string2)}") // false
    println("String Equal: ${string1.compareTo(string2)}") // -32
    println("String Equal: ${string1.equals(string3)}") // true
    println("String Equal: ${string1.compareTo(string3)}") // 0
    println("String Equal: ${string1 === string3}") //true

    //array creation

    val oneTo10: IntRange = 1..10
    val oneTo10VersionTwo = 1.rangeTo(10)

    val tenToOne: IntProgression = 10.downTo(1)

    val step = tenToOne.step(1)

    for (x in oneTo10) {
        println("range: $x")
    }


    for (x in step) {
        println("range: $x")
    }


    // boolean operator

    val age = 18

    if (age < 5) {
        println("less than 5")
    } else if (age == 5) {
        println("is 5")
    } else {
        println("greater than 5")
    }

    when (age) {
        1, 2, 3, 4, 5 -> print("print this when some of these are filled in")

        in 6..17 -> {
            print("execute this block of code")
        }

        25 -> test()


    }


}

fun comparsions() {
    val a: Int = 10000
    println(a === a) // Prints 'true'
    val boxedA: Int? = a
    val anotherBoxedA: Int? = a
    println(boxedA === anotherBoxedA) // !!!Prints 'false'!!! // compares references (sometimes) Whomp whomp
    println(boxedA == anotherBoxedA) // Prints 'true' //compares values
}

fun test(): Unit {

}


//default values


fun exampleDefaultValues(number: Int = 0, numberTwo: Int = 0): Int {
    return number + numberTwo
}

val exampleCallingDefault = exampleDefaultValues(1, 1) // 2
val exampleCallingDefaultTwo = exampleDefaultValues(1) // 1

//picking specfic param
val exampleCallingDefaultMissingParam = exampleDefaultValues(numberTwo = 1) // 1


// lambdas
//filter collection if even
fun containsEven(collection: Collection<Int>): Boolean = collection.any { x -> ((x % 2) == 0) }


//classes

//Data class
// equals/hashCode, toString and some others.
data class Person(val name: String, val age: Int)


// if blocks


fun demoIf(age: Int): Boolean {

    val allowedToWatch = if (age > 18) {
        print("older than a")
        true
    } else {
        print("Choose b")
        false
    }

    //used as switch case

    val list = arrayListOf(1, 2, 3)

    when (age) {


        0, 1 -> print("x == 0 or x == 1")
        in list -> print("it's in the list")
        in 1..10 -> print("x is in the range")
        !in 10..20 -> print("x is outside the range")

        //parseWhen(age) -> print("s encodes x")

        else -> { // Note the block
            print("x is neither 1 nor 2")
        }
    }


    return allowedToWatch
}


// loops

fun listDemo(): Unit {

    val ints = listOf(1, 2, 3, 4, 6)


    //inline
    for (item in ints) print(item)

    //block
    for (item: Int in ints) {
        print(item)
    }

    //iterate 1, 2 ,3
    for (i in 1..3) {
        println(i)
    }

    //iterate 6, 4 , 2 , 0
    for (i in 6 downTo 0 step 2) {
        println(i)
    }

    // iterate through an array or a list with an index
    for (i in ints.indices) {
        println(ints[i])
    }
    //Alternatively
    for ((index, value) in ints.withIndex()) {
        println("the element at $index is $value")
    }


    //While loops

    var x = 10

    while (x > 0) {
        x--
    }

    do {
        x--
        val y = x
    } while (y > 0) // y is visible here!


    //supports

//    return. By default returns from the nearest enclosing function or anonymous function.
//    break. Terminates the nearest enclosing loop.
//    continue. Proceeds to the next step of the nearest enclosing loop.

    loop@ for (i in 1..100) {
        for (j in 1..100) {
            if (Random.nextBoolean()) break@loop
        }
    }

    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return // non-local return directly to the caller of foo()
        print(it)
    }     //Prints 12


    //returns from lambda so looping continues
    listOf(1, 2, 3, 4, 5).forEach lit@{
        if (it == 3) return@lit // local return to the caller of the lambda, i.e. the forEach loop
        print(it)
    } //1245 done with explicit label

    //use as anonymous function gives same result
    listOf(1, 2, 3, 4, 5).forEach(fun(value: Int) {
        if (value == 3) return  // local return to the caller of the anonymous fun, i.e. the forEach loop
        print(value)
    }) //1245 done with explicit label


    val anonFunction = fun(x: Int): Int = x * 2

    val derp = { x: Int -> x * x }

    val testDerp = derp(1)


    //classes can have primary constructor and many secondary constructors


    // class constructor can be omitted without- public/private modifiers

    // with primary constructor
    // primary constructor cannot contain any code. only vars
    class PersonOne constructor(firstName: String) {}

    class PersonTwo(firstName: String) {}


    class exampleClass(name: String) {

        val firstProperty = "First property: $name".also(::println)

        //initializer block
        init {
            println("First initializer block that prints ${name}")
        }

    }

    //primariry constructor lets you declare variables in it.
    //Val for immutable , and var for variable data.

    class Person(val firstName: String, val lastName: String, var age: Int) {}

    val examplePersonClass = Person("Alex", "Troll", 18)
    val alexAge = examplePersonClass.age

    //If the constructor has annotations or visibility modifiers,
    // the constructor keyword is required, and the modifiers go before it:
    // class Customer public @Inject constructor(name: String) { ... }

    // secondary constructors
    class PersonSecondConstructor {
        val age: Int

        constructor(PersonSecondConstructor: PersonSecondConstructor, age: Int) {
            this.age = age
        }
    }

    //If the class has a primary constructor, each secondary constructor needs to delegate to the primary constructor,
    // done through this keyword


    class PersonPrimaryAndSecondary(val name: String) {
        constructor(name: String, parent: PersonPrimaryAndSecondary) : this(name) {
            //parent.children.add(this)
        }
    }

    // so the code in all initializer blocks is executed before the secondary constructor body.
    // init block runs first then constructor
    class Constructors() {
        init {
            println("Init block")
        }

        constructor(i: Int) : this() {
            println("Constructor")
        }
    }

    val testConstructorYolo = Constructors(1)


    //Creating instances of classes, Ie create object!
    //Note that Kotlin does not have a new keyword.
    val invoice = Constructors()

    val customer = Constructors(1)


    // inheretence


    //all classes in kotlin has any as super class
    class Example // Implicitly inherits from Any


    open class Base(p: Int)
    class Derived(p: Int) : Base(p)

    //If the derived class has a primary constructor,
    // the base class can (and must) be initialized right there,
    // using the parameters of the primary constructor.
    /*
    class MyView : View {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    }
     */

    // Overriding Methods
    open class BaseOverride {
        //open lets you override function
        open fun v() {
            print("hello Base")
        }

        fun nv() {}

        open val x = 2

    }

    class OverrideClass() : BaseOverride() {
        override fun v() {
            print("hello override")
        }
        // block sub classes from overriding it again.
        // final override fun v() {  }

        override val x = 10
    }


    open class BaseElpased(val name: String) {

        init {
            println("Initializing Base")
        }

        open val size: Int =
            name.length.also { println("Initializing size in Base: $it") }
    }

    class DerivedElpased(
        name: String, val lastName: String
    ) : BaseElpased(name.capitalize().also { println("Argument for Base: $it") }) {

        init {
            println("Initializing Derived")
        }

        override val size: Int =
            (super.size + lastName.length).also { println("Initializing size in Derived: $it") }
    }

    /*
    Constructing Derived("hello", "world")
    Argument for Base: Hello
    Initializing Base
    Initializing size in Base: 5
    Initializing Derived
    Initializing size in Derived: 10
     */

    /* (constructor-derived-class)
       (constructor-base-class)
       (init base-class block & vals)
       (init derived-class block & vals)
     */

    // therefore avoid using open members in the constructors, property initializers, and init blocks.

    // super to call parent implementation

    /*
    * class Bar : Foo() {
    override fun f() { /* ... */ }
    override val x: Int get() = 0

    inner class Baz {
        fun g() {
            super@Bar.f() // Calls Foo's implementation of f()
            println(super@Bar.x) // Uses Foo's implementation of x's getter
        }
    }
}
   */


}
