package app.runchallenge.examples


class MyClass1 {
    companion object {
        fun companionFun(): Unit {

        }
    }
}

object test {
    fun companionFun(): Int {
        return 0
    }
}

val callinCompanionFun = MyClass1.companionFun()


val callinCompanionFunTwo: Int = test.companionFun()

//a lambda expression:
val lambda = { a: Int, b: Int -> a + b }

//an anonymous function:
val anonumousFun = fun(s: String): Int { return s.toIntOrNull() ?: 0 }

fun MyClass1.Companion.foo() {
    print("foobar")
}


data class User(val name: String = "", val age: Int = 0)

//Deconstruction
val jane = User("Jane", 35)

fun local(): Unit {
    val (name, age) = jane
    println("$name, $age years of age") // prints "Jane, 35 years of age"
}

data class Results(val x: Int, val y: Int)

fun testDeconstruction(x: Int): Results {
    return Results(x, 1);
}

fun contuaition(): Unit {
    val (x, y) = app.runchallenge.examples.testDeconstruction(1)
}

