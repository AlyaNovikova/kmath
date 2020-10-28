package kscience.kmath.commons.prob

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kscience.kmath.prob.RandomGenerator
import kscience.kmath.prob.blocking
import kscience.kmath.prob.fromSource
import kscience.kmath.prob.samplers.GaussianSampler
import org.apache.commons.rng.simple.RandomSource
import java.time.Duration
import java.time.Instant
import org.apache.commons.rng.sampling.distribution.GaussianSampler as CMGaussianSampler
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler as CMZigguratNormalizedGaussianSampler

private suspend fun runKMathChained(): Duration {
    val generator = RandomGenerator.fromSource(RandomSource.MT, 123L)
    val normal = GaussianSampler.of(7.0, 2.0)
    val chain = normal.sample(generator).blocking()
    val startTime = Instant.now()
    var sum = 0.0

    repeat(10000001) { counter ->
        sum += chain.next()

        if (counter % 100000 == 0) {
            val duration = Duration.between(startTime, Instant.now())
            val meanValue = sum / counter
            println("Chain sampler completed $counter elements in $duration: $meanValue")
        }
    }

    return Duration.between(startTime, Instant.now())
}

private fun runApacheDirect(): Duration {
    val rng = RandomSource.create(RandomSource.MT, 123L)

    val sampler = CMGaussianSampler.of(
        CMZigguratNormalizedGaussianSampler.of(rng),
        7.0,
        2.0
    )

    val startTime = Instant.now()
    var sum = 0.0

    repeat(10000001) { counter ->
        sum += sampler.sample()

        if (counter % 100000 == 0) {
            val duration = Duration.between(startTime, Instant.now())
            val meanValue = sum / counter
            println("Direct sampler completed $counter elements in $duration: $meanValue")
        }
    }

    return Duration.between(startTime, Instant.now())
}

/**
 * Comparing chain sampling performance with direct sampling performance
 */
fun main(): Unit = runBlocking(Dispatchers.Default) {
    val chainJob = async { runKMathChained() }
    val directJob = async { runApacheDirect() }
    println("KMath Chained: ${chainJob.await()}")
    println("Apache Direct: ${directJob.await()}")
}