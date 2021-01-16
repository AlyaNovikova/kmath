package kscience.kmath.linear

/**
 * A marker interface representing some properties of matrices or additional transformations of them. Features are used
 * to optimize matrix operations performance in some cases or retrieve the APIs.
 */
public interface MatrixFeature

/**
 * Matrices with this feature are considered to have only diagonal non-null elements.
 */
public object DiagonalFeature : MatrixFeature

/**
 * Matrices with this feature have all zero elements.
 */
public object ZeroFeature : MatrixFeature

/**
 * Matrices with this feature have unit elements on diagonal and zero elements in all other places.
 */
public object UnitFeature : MatrixFeature

/**
 * Matrices with this feature can be inverted: [inverse] = `a`<sup>-1</sup> where `a` is the owning matrix.
 *
 * @param T the type of matrices' items.
 */
public interface InverseMatrixFeature<T : Any> : MatrixFeature {
    /**
     * The inverse matrix of the matrix that owns this feature.
     */
    public val inverse: FeaturedMatrix<T>
}

/**
 * Matrices with this feature can compute their determinant.
 */
public interface DeterminantFeature<T : Any> : MatrixFeature {
    /**
     * The determinant of the matrix that owns this feature.
     */
    public val determinant: T
}

/**
 * Produces a [DeterminantFeature] where the [DeterminantFeature.determinant] is [determinant].
 *
 * @param determinant the value of determinant.
 * @return a new [DeterminantFeature].
 */
@Suppress("FunctionName")
public fun <T : Any> DeterminantFeature(determinant: T): DeterminantFeature<T> = object : DeterminantFeature<T> {
    override val determinant: T = determinant
}

/**
 * Matrices with this feature are lower triangular ones.
 */
public object LFeature : MatrixFeature

/**
 * Matrices with this feature are upper triangular ones.
 */
public object UFeature : MatrixFeature

/**
 * Matrices with this feature support LU factorization with partial pivoting: *[p] &middot; a = [l] &middot; [u]* where
 * *a* is the owning matrix.
 *
 * @param T the type of matrices' items.
 */
public interface LupDecompositionFeature<T : Any> : MatrixFeature {
    /**
     * The lower triangular matrix in this decomposition. It may have [LFeature].
     */
    public val l: FeaturedMatrix<T>

    /**
     * The upper triangular matrix in this decomposition. It may have [UFeature].
     */
    public val u: FeaturedMatrix<T>

    /**
     * The permutation matrix in this decomposition.
     */
    public val p: FeaturedMatrix<T>
}

/**
 * Matrices with this feature are orthogonal ones: *a &middot; a<sup>T</sup> = u* where *a* is the owning matrix, *u*
 * is the unit matrix ([UnitFeature]).
 */
public object OrthogonalFeature : MatrixFeature

/**
 * Matrices with this feature support QR factorization: *a = [q] &middot; [r]* where *a* is the owning matrix.
 *
 * @param T the type of matrices' items.
 */
public interface QRDecompositionFeature<T : Any> : MatrixFeature {
    /**
     * The orthogonal matrix in this decomposition. It may have [OrthogonalFeature].
     */
    public val q: FeaturedMatrix<T>

    /**
     * The upper triangular matrix in this decomposition. It may have [UFeature].
     */
    public val r: FeaturedMatrix<T>
}

/**
 * Matrices with this feature support Cholesky factorization: *a = [l] &middot; [l]<sup>H</sup>* where *a* is the
 * owning matrix.
 *
 * @param T the type of matrices' items.
 */
public interface CholeskyDecompositionFeature<T : Any> : MatrixFeature {
    /**
     * The triangular matrix in this decomposition. It may have either [UFeature] or [LFeature].
     */
    public val l: FeaturedMatrix<T>
}

/**
 * Matrices with this feature support SVD: *a = [u] &middot; [s] &middot; [v]<sup>H</sup>* where *a* is the owning
 * matrix.
 *
 * @param T the type of matrices' items.
 */
public interface SingularValueDecompositionFeature<T : Any> : MatrixFeature {
    /**
     * The matrix in this decomposition. It is unitary, and it consists from left singular vectors.
     */
    public val u: FeaturedMatrix<T>

    /**
     * The matrix in this decomposition. Its main diagonal elements are singular values.
     */
    public val s: FeaturedMatrix<T>

    /**
     * The matrix in this decomposition. It is unitary, and it consists from right singular vectors.
     */
    public val v: FeaturedMatrix<T>

    /**
     * The buffer of singular values of this SVD.
     */
    public val singularValues: Point<T>
}

//TODO add sparse matrix feature
