package kscience.kmath.structures

/**
 * A structure that is guaranteed to be two-dimensional.
 *
 * @param T the type of items.
 */
public interface Structure2D<T> : NDStructure<T> {
    /**
     * The number of rows in this structure.
     */
    public val rowNum: Int get() = shape[0]

    /**
     * The number of columns in this structure.
     */
    public val colNum: Int get() = shape[1]

    /**
     * The buffer of rows of this structure. It gets elements from the structure dynamically.
     */
    public val rows: Buffer<Buffer<T>>
        get() = VirtualBuffer(rowNum) { i -> VirtualBuffer(colNum) { j -> get(i, j) } }

    /**
     * The buffer of columns of this structure. It gets elements from the structure dynamically.
     */
    public val columns: Buffer<Buffer<T>>
        get() = VirtualBuffer(colNum) { j -> VirtualBuffer(rowNum) { i -> get(i, j) } }

    /**
     * Retrieves an element from the structure by two indices.
     *
     * @param i the first index.
     * @param j the second index.
     * @return an element.
     */
    public operator fun get(i: Int, j: Int): T

    override operator fun get(index: IntArray): T {
        require(index.size == 2) { "Index dimension mismatch. Expected 2 but found ${index.size}" }
        return get(index[0], index[1])
    }

    override fun elements(): Sequence<Pair<IntArray, T>> = sequence {
        for (i in 0 until rowNum)
            for (j in 0 until colNum) yield(intArrayOf(i, j) to get(i, j))
    }

    public companion object
}

/**
 * A 2D wrapper for nd-structure
 */
private inline class Structure2DWrapper<T>(val structure: NDStructure<T>) : Structure2D<T> {
    override val shape: IntArray get() = structure.shape

    override operator fun get(i: Int, j: Int): T = structure[i, j]

    override fun elements(): Sequence<Pair<IntArray, T>> = structure.elements()
}

/**
 * Represent a [NDStructure] as [Structure1D]. Throw error in case of dimension mismatch
 */
public fun <T> NDStructure<T>.as2D(): Structure2D<T> = if (shape.size == 2)
    Structure2DWrapper(this)
else
    error("Can't create 2d-structure from ${shape.size}d-structure")

/**
 * Alias for [Structure2D] with more familiar name.
 *
 * @param T the type of items.
 */
public typealias Matrix<T> = Structure2D<T>
