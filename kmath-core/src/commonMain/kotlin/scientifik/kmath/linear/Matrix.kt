package scientifik.kmath.linear

import scientifik.kmath.operations.RealField
import scientifik.kmath.operations.Ring
import scientifik.kmath.structures.*
import scientifik.kmath.structures.Buffer.Companion.DoubleBufferFactory
import scientifik.kmath.structures.Buffer.Companion.boxing
import kotlin.math.sqrt


interface MatrixContext<T : Any> {
    /**
     * Produce a matrix with this context and given dimensions
     */
    fun produce(rows: Int, columns: Int, initializer: (i: Int, j: Int) -> T): Matrix<T>

    infix fun Matrix<T>.dot(other: Matrix<T>): Matrix<T>

    infix fun Matrix<T>.dot(vector: Point<T>): Point<T>

    operator fun Matrix<T>.unaryMinus(): Matrix<T>

    operator fun Matrix<T>.plus(b: Matrix<T>): Matrix<T>

    operator fun Matrix<T>.minus(b: Matrix<T>): Matrix<T>

    operator fun Matrix<T>.times(value: T): Matrix<T>

    operator fun T.times(m: Matrix<T>): Matrix<T> = m * this

    companion object {
        /**
         * Non-boxing double matrix
         */
        val real = BufferMatrixContext(RealField, DoubleBufferFactory)

        /**
         * A structured matrix with custom buffer
         */
        fun <T : Any, R : Ring<T>> buffered(
            ring: R,
            bufferFactory: BufferFactory<T> = ::boxing
        ): GenericMatrixContext<T, R> =
            BufferMatrixContext(ring, bufferFactory)

        /**
         * Automatic buffered matrix, unboxed if it is possible
         */
        inline fun <reified T : Any, R : Ring<T>> auto(ring: R): GenericMatrixContext<T, R> =
            buffered(ring, Buffer.Companion::auto)
    }
}

interface GenericMatrixContext<T : Any, R : Ring<T>> : MatrixContext<T> {
    /**
     * The ring context for matrix elements
     */
    val elementContext: R

    /**
     * Produce a point compatible with matrix space
     */
    fun point(size: Int, initializer: (Int) -> T): Point<T>

    override infix fun Matrix<T>.dot(other: Matrix<T>): Matrix<T> {
        //TODO add typed error
        if (this.colNum != other.rowNum) error("Matrix dot operation dimension mismatch: ($rowNum, $colNum) x (${other.rowNum}, ${other.colNum})")
        return produce(rowNum, other.colNum) { i, j ->
            val row = rows[i]
            val column = other.columns[j]
            with(elementContext) {
                row.asSequence().zip(column.asSequence(), ::multiply).sum()
            }
        }
    }

    override infix fun Matrix<T>.dot(vector: Point<T>): Point<T> {
        //TODO add typed error
        if (this.colNum != vector.size) error("Matrix dot vector operation dimension mismatch: ($rowNum, $colNum) x (${vector.size})")
        return point(rowNum) { i ->
            val row = rows[i]
            with(elementContext) {
                row.asSequence().zip(vector.asSequence(), ::multiply).sum()
            }
        }
    }

    override operator fun Matrix<T>.unaryMinus() =
        produce(rowNum, colNum) { i, j -> elementContext.run { -get(i, j) } }

    override operator fun Matrix<T>.plus(b: Matrix<T>): Matrix<T> {
        if (rowNum != b.rowNum || colNum != b.colNum) error("Matrix operation dimension mismatch. [$rowNum,$colNum] + [${b.rowNum},${b.colNum}]")
        return produce(rowNum, colNum) { i, j -> elementContext.run { get(i, j) + b[i, j] } }
    }

    override operator fun Matrix<T>.minus(b: Matrix<T>): Matrix<T> {
        if (rowNum != b.rowNum || colNum != b.colNum) error("Matrix operation dimension mismatch. [$rowNum,$colNum] - [${b.rowNum},${b.colNum}]")
        return produce(rowNum, colNum) { i, j -> elementContext.run { get(i, j) + b[i, j] } }
    }

    operator fun Matrix<T>.times(number: Number): Matrix<T> =
        produce(rowNum, colNum) { i, j -> elementContext.run { get(i, j) * number } }

    operator fun Number.times(matrix: Matrix<T>): Matrix<T> = matrix * this

    override fun Matrix<T>.times(value: T): Matrix<T> =
        produce(rowNum, colNum) { i, j -> elementContext.run { get(i, j) * value } }
}

/**
 * Specialized 2-d structure
 */
interface Matrix<T : Any> : NDStructure<T> {
    val rowNum: Int
    val colNum: Int

    val features: Set<MatrixFeature>

    /**
     * Suggest new feature for this matrix. The result is the new matrix that may or may not reuse existing data structure.
     *
     * The implementation does not guarantee to check that matrix actually have the feature, so one should be careful to
     * add only those features that are valid.
     */
    fun suggestFeature(vararg features: MatrixFeature): Matrix<T>

    operator fun get(i: Int, j: Int): T

    override fun get(index: IntArray): T = get(index[0], index[1])

    override val shape: IntArray get() = intArrayOf(rowNum, colNum)

    val rows: Point<Point<T>>
        get() = VirtualBuffer(rowNum) { i ->
            VirtualBuffer(colNum) { j -> get(i, j) }
        }

    val columns: Point<Point<T>>
        get() = VirtualBuffer(colNum) { j ->
            VirtualBuffer(rowNum) { i -> get(i, j) }
        }

    override fun elements(): Sequence<Pair<IntArray, T>> = sequence {
        for (i in (0 until rowNum)) {
            for (j in (0 until colNum)) {
                yield(intArrayOf(i, j) to get(i, j))
            }
        }
    }

    companion object {
        fun real(rows: Int, columns: Int, initializer: (Int, Int) -> Double) =
            MatrixContext.real.produce(rows, columns, initializer)

        /**
         * Build a square matrix from given elements.
         */
        fun <T : Any> square(vararg elements: T): Matrix<T> {
            val size: Int = sqrt(elements.size.toDouble()).toInt()
            if (size * size != elements.size) error("The number of elements ${elements.size} is not a full square")
            val buffer = elements.asBuffer()
            return BufferMatrix(size, size, buffer)
        }

        fun <T : Any> build(rows: Int, columns: Int): MatrixBuilder<T> = MatrixBuilder(rows, columns)
    }
}

class MatrixBuilder<T : Any>(val rows: Int, val columns: Int) {
    operator fun invoke(vararg elements: T): Matrix<T> {
        if (rows * columns != elements.size) error("The number of elements ${elements.size} is not equal $rows * $columns")
        val buffer = elements.asBuffer()
        return BufferMatrix(rows, columns, buffer)
    }
}

/**
 * Check if matrix has the given feature class
 */
inline fun <reified T : Any> Matrix<*>.hasFeature(): Boolean = features.find { T::class.isInstance(it) } != null

/**
 * Get the first feature matching given class. Does not guarantee that matrix has only one feature matching the criteria
 */
inline fun <reified T : Any> Matrix<*>.getFeature(): T? = features.filterIsInstance<T>().firstOrNull()

/**
 * Diagonal matrix of ones. The matrix is virtual no actual matrix is created
 */
fun <T : Any, R : Ring<T>> GenericMatrixContext<T, R>.one(rows: Int, columns: Int): Matrix<T> =
    VirtualMatrix<T>(rows, columns) { i, j ->
        if (i == j) elementContext.one else elementContext.zero
    }


/**
 * A virtual matrix of zeroes
 */
fun <T : Any, R : Ring<T>> GenericMatrixContext<T, R>.zero(rows: Int, columns: Int): Matrix<T> =
    VirtualMatrix<T>(rows, columns) { i, j ->
        elementContext.zero
    }

class TransposedFeature<T : Any>(val original: Matrix<T>) : MatrixFeature

/**
 * Create a virtual transposed matrix without copying anything. `A.transpose().transpose() === A`
 */
fun <T : Any, R : Ring<T>> Matrix<T>.transpose(): Matrix<T> {
    return this.getFeature<TransposedFeature<T>>()?.original ?: VirtualMatrix(
        this.colNum,
        this.rowNum,
        setOf(TransposedFeature(this))
    ) { i, j -> get(j, i) }
}

infix fun Matrix<Double>.dot(other: Matrix<Double>): Matrix<Double> = with(MatrixContext.real) { dot(other) }