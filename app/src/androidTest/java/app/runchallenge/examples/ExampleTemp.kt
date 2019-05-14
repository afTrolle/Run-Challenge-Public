package app.runchallenge.examples


open class Human(
    val age: Int = 0
)


class ChildOneExample() : Human() {

    fun printValue() {
        print(age)
    }

}


class ChildTwo : Human() {

}

fun compareChildren(human1: Human, human2: Human): Unit {

    if (human1.age > human2.age) {

    }
}



fun usingCompare() {
    compareChildren(ChildOneExample(), ChildTwo())
}