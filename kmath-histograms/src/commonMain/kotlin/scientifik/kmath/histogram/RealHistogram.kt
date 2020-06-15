package scientifik.kmath.histogram

import scientifik.kmath.linear.Point
import scientifik.kmath.real.asVector
import scientifik.kmath.operations.SpaceOperations
import scientifik.kmath.structures.*
import kotlin.math.floor


data class BinDef<T : Comparable<T>>(val space: SpaceOperations<Point<T>>, val center: Point<T>, val sizes: Point<T>) {
    fun contains(vector: Point<out T>): Boolean {
        if (vector.size != center.size) error("Dimension mismatch for input vector. Expected ${center.size}, but found ${vector.size}")
        val upper = space.run { center + sizes / 2.0 }
        val lower = space.run { center - sizes / 2.0 }
        return vector.asSequence().mapIndexed { i, value ->
            value in lower[i]..upper[i]
        }.all { it }
    }
}


class MultivariateBin<T : Comparable<T>>(val def: BinDef<T>, override val value: Number) : Bin<T> {

    override fun contains(vector: Point<out T>): Boolean = def.contains(vector)

    override val dimension: Int
        get() = def.center.size

    override val center: Point<T>
        get() = def.center

}

/**
 * Uniform multivariate histogram with fixed borders. Based on NDStructure implementation with complexity of m for bin search, where m is the number of dimensions.
 */
class RealHistogram(
    private val lower: Buffer<Double>,
    private val upper: Buffer<Double>,
    private val binNums: IntArray = IntArray(lower.size) { 20 }
) : MutableHistogram<Double, MultivariateBin<Double>> {


    private val strides = DefaultStrides(IntArray(binNums.size) { binNums[it] + 2 })

    private val values: NDStructure<LongCounter> = NDStructure.auto(strides) { LongCounter() }

    private val weights: NDStructure<DoubleCounter> = NDStructure.auto(strides) { DoubleCounter() }

    override val dimension: Int get() = lower.size


    private val binSize = DoubleBuffer(dimension) { (upper[it] - lower[it]) / binNums[it] }

    init {
        // argument checks
        if (lower.size != upper.size) error("Dimension mismatch in histogram lower and upper limits.")
        if (lower.size != binNums.size) error("Dimension mismatch in bin count.")
        if ((0 until dimension).any { upper[it] - lower[it] < 0 }) error("Range for one of axis is not strictly positive")
    }


    /**
     * Get internal [NDStructure] bin index for given axis
     */
    private fun getIndex(axis: Int, value: Double): Int {
        return when {
            value >= upper[axis] -> binNums[axis] + 1 // overflow
            value < lower[axis] -> 0 // underflow
            else -> floor((value - lower[axis]) / binSize[axis]).toInt() + 1
        }
    }

    private fun getIndex(point: Buffer<out Double>): IntArray = IntArray(dimension) { getIndex(it, point[it]) }

    private fun getValue(index: IntArray): Long {
        return values[index].sum()
    }

    fun getValue(point: Buffer<out Double>): Long {
        return getValue(getIndex(point))
    }

    private fun getDef(index: IntArray): BinDef<Double> {
        val center = index.mapIndexed { axis, i ->
            when (i) {
                0 -> Double.NEGATIVE_INFINITY
                strides.shape[axis] - 1 -> Double.POSITIVE_INFINITY
                else -> lower[axis] + (i.toDouble() - 0.5) * binSize[axis]
            }
        }.asBuffer()
        return BinDef(RealBufferFieldOperations, center, binSize)
    }

    fun getDef(point: Buffer<out Double>): BinDef<Double> {
        return getDef(getIndex(point))
    }

    override fun get(point: Buffer<out Double>): MultivariateBin<Double>? {
        val index = getIndex(point)
        return MultivariateBin(getDef(index), getValue(index))
    }

//    fun put(point: Point<out Double>){
//        val index = getIndex(point)
//        values[index].increment()
//    }

    override fun putWithWeight(point: Buffer<out Double>, weight: Double) {
        val index = getIndex(point)
        values[index].increment()
        weights[index].add(weight)
    }

    override fun iterator(): Iterator<MultivariateBin<Double>> = weights.elements().map { (index, value) ->
        MultivariateBin(getDef(index), value.sum())
    }.iterator()

    /**
     * Convert this histogram into NDStructure containing bin values but not bin descriptions
     */
    fun values(): NDStructure<Number> {
        return NDStructure.auto(values.shape) { values[it].sum() }
    }

    /**
     * Sum of weights
     */
    fun weights():NDStructure<Double>{
        return NDStructure.auto(weights.shape) { weights[it].sum() }
    }

    companion object {

        /**
         * Use it like
         * ```
         *FastHistogram.fromRanges(
         *  (-1.0..1.0),
         *  (-1.0..1.0)
         *)
         *```
         */
        fun fromRanges(vararg ranges: ClosedFloatingPointRange<Double>): RealHistogram {
            return RealHistogram(
                ranges.map { it.start }.asVector(),
                ranges.map { it.endInclusive }.asVector()
            )
        }

        /**
         * Use it like
         * ```
         *FastHistogram.fromRanges(
         *  (-1.0..1.0) to 50,
         *  (-1.0..1.0) to 32
         *)
         *```
         */
        fun fromRanges(vararg ranges: Pair<ClosedFloatingPointRange<Double>, Int>): RealHistogram {
            return RealHistogram(
                ListBuffer(ranges.map { it.first.start }),
                ListBuffer(ranges.map { it.first.endInclusive }),
                ranges.map { it.second }.toIntArray()
            )
        }
    }

}