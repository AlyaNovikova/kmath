package scientifik.kmath.prob

import kotlinx.atomicfu.atomic
import scientifik.kmath.chains.Chain

/**
 * A possibly stateful chain producing random values.
 */
class RandomChain<out R>(val generator: RandomGenerator, private val gen: suspend RandomGenerator.() -> R) : Chain<R> {
    private val atomicValue = atomic<R?>(null)

    override suspend fun next(): R = generator.gen().also { atomicValue.lazySet(it) }

    override fun fork(): Chain<R> = RandomChain(generator.fork(), gen)
}

/**
 * Create a chain of doubles from generator after forking it so the chain is not affected by operations on generator
 */
fun RandomGenerator.doubles(): Chain<Double> = RandomChain(fork()) { nextDouble() }