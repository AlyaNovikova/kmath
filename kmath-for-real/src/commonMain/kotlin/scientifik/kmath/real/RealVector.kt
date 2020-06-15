package scientifik.kmath.real

import scientifik.kmath.linear.BufferVectorSpace
import scientifik.kmath.linear.Point
import scientifik.kmath.linear.VectorSpace
import scientifik.kmath.operations.Norm
import scientifik.kmath.operations.RealField
import scientifik.kmath.operations.SpaceElement
import scientifik.kmath.structures.Buffer
import scientifik.kmath.structures.DoubleBuffer
import scientifik.kmath.structures.asBuffer
import scientifik.kmath.structures.asIterable
import kotlin.math.sqrt

fun DoubleArray.asVector() = RealVector(this.asBuffer())
fun List<Double>.asVector() = RealVector(this.asBuffer())


object VectorL2Norm : Norm<Point<out Number>, Double> {
    override fun norm(arg: Point<out Number>): Double = sqrt(arg.asIterable().sumByDouble { it.toDouble() })
}

inline class RealVector(private val point: Point<Double>) :
    SpaceElement<Point<Double>, RealVector, VectorSpace<Double, RealField>>, Point<Double> {

    override val context: VectorSpace<Double, RealField>
        get() = space(
            point.size
        )

    override fun unwrap(): Point<Double> = point

    override fun Point<Double>.wrap(): RealVector =
        RealVector(this)

    override val size: Int get() = point.size

    override fun get(index: Int): Double = point[index]

    override fun iterator(): Iterator<Double> = point.iterator()

    companion object {

        private val spaceCache = HashMap<Int, BufferVectorSpace<Double, RealField>>()

        inline operator fun invoke(dim: Int, initializer: (Int) -> Double) =
            RealVector(DoubleBuffer(dim, initializer))

        operator fun invoke(vararg values: Double): RealVector = values.asVector()

        fun space(dim: Int): BufferVectorSpace<Double, RealField> =
            spaceCache.getOrPut(dim) {
                BufferVectorSpace(
                    dim,
                    RealField
                ) { size, init -> Buffer.real(size, init) }
            }
    }
}