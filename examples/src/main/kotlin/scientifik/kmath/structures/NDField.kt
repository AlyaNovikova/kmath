package scientifik.kmath.structures

import kotlinx.coroutines.GlobalScope
import scientifik.kmath.operations.RealField
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val dim = 1000
    val n = 1000

    // automatically build coroutineContext most suited for given type.
    val autoField = NDField.auto(RealField, dim, dim)
    // specialized nd-field for Double. It works as generic Double field as well
    val specializedField = NDField.real(dim, dim)
    //A generic boxing field. It should be used for objects, not primitives.
    val genericField = NDField.boxing(RealField, dim, dim)


    val autoTime = measureTimeMillis {
        autoField.run {
            var res = one
            repeat(n) {
                res += 1.0
            }
        }
    }

    println("Automatic field addition completed in $autoTime millis")

    val elementTime = measureTimeMillis {
        var res = genericField.one
        repeat(n) {
            res += 1.0
        }
    }

    println("Element addition completed in $elementTime millis")

    val specializedTime = measureTimeMillis {
        specializedField.run {
            var res: NDBuffer<Double> = one
            repeat(n) {
                res += 1.0
            }
        }
    }

    println("Specialized addition completed in $specializedTime millis")


    val lazyTime = measureTimeMillis {
        val res = specializedField.one.mapAsync(GlobalScope) {
            var c = 0.0
            repeat(n) {
                c += 1.0
            }
            c
        }

        res.elements().forEach { it.second }
    }

    println("Lazy addition completed in $lazyTime millis")

    val genericTime = measureTimeMillis {
        //genericField.run(action)
        genericField.run {
            var res: NDBuffer<Double> = one
            repeat(n) {
                res += 1.0
            }
        }
    }

    println("Generic addition completed in $genericTime millis")

}